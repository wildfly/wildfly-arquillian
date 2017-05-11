/*
 * Copyright 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.arquillian.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.logging.Logger;

/**
 * Observes the {@link BeforeDeploy}, {@link AfterUnDeploy} and {@link AfterClass} lifecycle events to ensure
 * {@linkplain ServerSetupTask setup tasks} are executed.
 *
 * @author Stuart Douglas
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings({"unused", "InstanceVariableMayNotBeInitialized"})
public class ServerSetupObserver {

    private static final Logger log = Logger.getLogger(ServerSetupObserver.class);

    @Inject
    private Instance<ManagementClient> managementClient;

    @Inject
    private Instance<ClassContext> classContextInstance;

    private final Map<String, ServerSetupTaskHolder> setupTasks = new HashMap<>();
    private boolean afterClassRun = false;

    /**
     * Observed only for state changes.
     *
     * @param beforeClass the lifecycle event
     */
    public synchronized void handleBeforeClass(@Observes BeforeClass beforeClass) {
        afterClassRun = false;
    }

    /**
     * Executed before deployments to lazily execute the {@link ServerSetupTask#setup(ManagementClient, String) ServerSetupTask}.
     * <p>
     * This is lazily loaded for manual mode tests. The server may not have been started at the
     * {@link org.jboss.arquillian.test.spi.event.suite.BeforeClass BeforeClass} event.
     * </p>
     *
     * @param event     the lifecycle event
     * @param container the container the event is being invoked on
     *
     * @throws Throwable if an error occurs processing the event
     */
    public synchronized void handleBeforeDeployment(@Observes BeforeDeploy event, Container container) throws Throwable {
        final String containerName = container.getName();
        if (setupTasks.containsKey(containerName)) {
            setupTasks.get(containerName).deployments.add(event.getDeployment());
            return;
        }

        final ClassContext classContext = classContextInstance.get();
        if (classContext == null) {
            return;
        }

        final Class<?> currentClass = classContext.getActiveId();

        ServerSetup setup = currentClass.getAnnotation(ServerSetup.class);
        if (setup == null) {
            return;
        }

        final ManagementClient client = managementClient.get();
        final ServerSetupTaskHolder holder = new ServerSetupTaskHolder(client);
        holder.deployments.add(event.getDeployment());
        setupTasks.put(containerName, holder);
        holder.setup(setup, containerName);
    }

    /**
     * Executed after the test class has completed. This ensures that any
     * {@linkplain ServerSetupTask#tearDown(ManagementClient, String) tear down tasks} have been executed if all
     * all deployments have been undeployed.
     *
     * @param afterClass the lifecycle event
     *
     * @throws Exception if an error occurs processing the event
     */
    public synchronized void afterTestClass(@Observes AfterClass afterClass) throws Exception {
        if (setupTasks.isEmpty()) {
            return;
        }

        // Clean up any remaining tasks from unmanaged deployments
        final Iterator<Map.Entry<String, ServerSetupTaskHolder>> iter = setupTasks.entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<String, ServerSetupTaskHolder> entry = iter.next();
            final ServerSetupTaskHolder holder = entry.getValue();
            // Only tearDown if all deployments have been removed from the container
            if (holder.deployments.isEmpty()) {
                entry.getValue().tearDown(entry.getKey());
                iter.remove();
            }
        }
        afterClassRun = true;
    }

    /**
     * Executed after each undeploy for the container.
     *
     * @param afterDeploy the lifecycle event
     * @param container   the container the event is being invoked on
     *
     * @throws Exception if an error occurs processing the event
     */
    public synchronized void handleAfterUndeploy(@Observes AfterUnDeploy afterDeploy, final Container container) throws Exception {
        final String containerName = container.getName();
        final ServerSetupTaskHolder holder = setupTasks.get(containerName);
        if (holder == null) {
            return;
        }

        // Remove the deployment
        if (holder.deployments.remove(afterDeploy.getDeployment())) {
            // If the deployments are now empty and the AfterClass has been invoked we need to ensure the tearDown() has
            // happened. This should clean up any tasks left from managed deployments or unmanaged deployments that were
            // not undeployed manually.
            if (afterClassRun && holder.deployments.isEmpty()) {
                holder.tearDown(containerName);
                setupTasks.remove(containerName);
            }
        }
    }

    private static class ServerSetupTaskHolder {
        private final ManagementClient client;
        private final Deque<ServerSetupTask> setupTasks;
        private final Set<DeploymentDescription> deployments;

        private ServerSetupTaskHolder(final ManagementClient client) {
            this.client = client;
            setupTasks = new ArrayDeque<>();
            deployments = new HashSet<>();
        }

        void setup(final ServerSetup setup, final String containerName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
            final Class<? extends ServerSetupTask>[] classes = setup.value();
            for (Class<? extends ServerSetupTask> clazz : classes) {
                final Constructor<? extends ServerSetupTask> ctor = clazz.getDeclaredConstructor();
                ctor.setAccessible(true);
                final ServerSetupTask task = ctor.newInstance();
                setupTasks.add(task);
                try {
                    task.setup(client, containerName);
                } catch (Throwable e) {
                    log.errorf(e, "Setup failed during setup. Offending class '%s'", task);
                }
            }
        }

        public void tearDown(final String containerName) {
            if (client.isClosed()) {
                log.errorf("The container '%s' may have been stopped. The management client has been closed and " +
                        "tearing down setup tasks is not possible.", containerName);
            } else {
                ServerSetupTask task;
                while ((task = setupTasks.pollLast()) != null) {
                    try {
                        task.tearDown(client, containerName);
                    } catch (Exception e) {
                        log.errorf(e, "Setup task failed during tear down. Offending class '%s'", task);
                    }
                }
            }
        }

        @Override
        public String toString() {
            return ServerSetupTaskHolder.class.getName() +
                    "[setupTasks=" +
                    setupTasks +
                    ", deployments" +
                    deployments +
                    "]";
        }
    }
}

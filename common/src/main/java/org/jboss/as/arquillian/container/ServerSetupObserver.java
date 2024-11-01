/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.event.enrichment.AfterEnrichment;
import org.jboss.arquillian.test.spi.event.enrichment.BeforeEnrichment;
import org.jboss.arquillian.test.spi.event.enrichment.EnrichmentEvent;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.as.arquillian.api.ReloadIfRequired;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.logging.Logger;
import org.wildfly.plugin.tools.server.ServerManager;

/**
 * Observes the {@link BeforeDeploy}, {@link AfterUnDeploy} and {@link AfterClass} lifecycle events to ensure
 * {@linkplain ServerSetupTask setup tasks} are executed.
 *
 * @author Stuart Douglas
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
public class ServerSetupObserver {

    private static final Logger log = Logger.getLogger(ServerSetupObserver.class);

    @Inject
    private Instance<ContainerContext> containerContext;

    @Inject
    private Instance<ManagementClient> managementClient;

    @Inject
    private Instance<ClassContext> classContextInstance;

    @Inject
    private Instance<ServiceLoader> serviceLoader;

    @Inject
    private Event<EnrichmentEvent> enrichmentEvent;

    @Inject
    private Instance<ServerManager> serverManager;

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
     * Executed before deployments to lazily execute the {@link ServerSetupTask#setup(ManagementClient, String)
     * ServerSetupTask}.
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
        final ServerSetupTaskHolder holder = new ServerSetupTaskHolder(serverManager.get(), client, container.getName());
        executeSetup(holder, setup, containerName, event.getDeployment());
    }

    /**
     * Executed after the test class has completed. This ensures that any
     * {@linkplain ServerSetupTask#tearDown(ManagementClient, String) tear down tasks} have been executed if
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
    public synchronized void handleAfterUndeploy(@Observes AfterUnDeploy afterDeploy, final Container container)
            throws Exception {
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

    private void executeSetup(final ServerSetupTaskHolder holder, ServerSetup setup, String containerName,
            DeploymentDescription deployment)
            throws Exception {

        holder.deployments.add(deployment);
        setupTasks.put(containerName, holder);

        try {
            holder.setup(setup, containerName);
        } catch (Throwable t) {
            final Throwable toThrow = t;
            // We're going to throw on the underlying problem. But since that is going to
            // propagate to surefire and prevent further processing of the currently executing
            // test class, first we need to do cleanup work that's normally triggered by
            // handleAfterUndeploy and afterTestClass calls that won't be happening.

            Object failedSetup = null;
            try {
                // Run tearDown on any task that already successfully completed setup

                // The last setup is the one that just failed. We skip calling tearDown on it
                // by popping it off the holder's setupTasks queue.
                // As noted in the ServerSetupTask.setup javadoc, implementations that
                // throw assumption failure exceptions should do so before making any changes
                // that would normally be reversed in a call to tearDown
                failedSetup = holder.setupTasks.pollLast();

                // Tell the holder to do the normal tearDown
                holder.tearDown(containerName);
            } catch (Exception logged) { // just to be safe
                String className = failedSetup != null ? failedSetup.getClass().getName()
                        : ServerSetupTask.class.getSimpleName();
                final String message = String
                        .format("Failed tearing down ServerSetupTasks after a failed assumption in %s.setup()", className);
                toThrow.addSuppressed(new RuntimeException(message, logged));
                log.errorf(logged, message);
            } finally {
                // Clean out our own state changes we made before calling holder.setup.
                // Otherwise, later classes that use the same container may fail.
                holder.deployments.remove(deployment);
                if (holder.deployments.isEmpty()) {
                    setupTasks.remove(containerName);
                }
            }
            if (toThrow instanceof Exception) {
                throw (Exception) toThrow;
            }
            if (toThrow instanceof Error) {
                throw (Error) toThrow;
            }
            // Throw the error as an assertion error to abort the testing
            throw new AssertionError("Failed to invoke a ServerSetupTask.", toThrow);
        }
    }

    private class ServerSetupTaskHolder {

        private final ManagementClient client;
        private final ServerManager serverManager;
        private final Deque<ServerSetupTask> setupTasks;
        private final Set<DeploymentDescription> deployments;
        private final String containerName;;

        private ServerSetupTaskHolder(final ServerManager serverManager, final ManagementClient client,
                final String containerName) {
            this.client = client;
            this.serverManager = serverManager;
            setupTasks = new ArrayDeque<>();
            deployments = new HashSet<>();
            this.containerName = containerName;
        }

        void setup(final ServerSetup setup, final String containerName) throws Throwable {
            final Class<? extends ServerSetupTask>[] classes = setup.value();
            for (Class<? extends ServerSetupTask> clazz : classes) {
                final Constructor<? extends ServerSetupTask> ctor = clazz.getDeclaredConstructor();
                ctor.setAccessible(true);
                final ServerSetupTask task = ctor.newInstance();
                enrich(task, clazz.getMethod("setup", ManagementClient.class, String.class));
                setupTasks.add(task);
                try {
                    task.setup(client, containerName);
                } finally {
                    if (task.getClass().isAnnotationPresent(ReloadIfRequired.class) && serverManager != null) {
                        final ReloadIfRequired reloadIfRequired = task.getClass().getAnnotation(ReloadIfRequired.class);
                        serverManager.reloadIfRequired(reloadIfRequired.value(), reloadIfRequired.timeUnit());
                    }
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
                        enrich(task, task.getClass().getMethod("tearDown", ManagementClient.class, String.class));
                        task.tearDown(client, containerName);
                    } catch (Throwable e) {
                        // Unlike with setup, here we don't propagate assumption failures.
                        // Whatever was meant to be turned off by an assumption failure in setup has
                        // already been turned off; here we want to ensure all tear down work proceeds.

                        log.errorf(e, "Setup task failed during tear down. Offending class '%s'", task);
                    } finally {
                        if (task.getClass().isAnnotationPresent(ReloadIfRequired.class) && serverManager != null) {
                            try {
                                final ReloadIfRequired reloadIfRequired = task.getClass().getAnnotation(ReloadIfRequired.class);
                                serverManager.reloadIfRequired(reloadIfRequired.value(), reloadIfRequired.timeUnit());
                            } catch (IOException e) {
                                log.errorf(e, "Failed to reload server. The server may still be in reload-required state.");
                            }
                        }
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

        private void enrich(final ServerSetupTask task, final Method method) {
            try {
                containerContext.get().activate(containerName);
                enrichmentEvent.fire(new BeforeEnrichment(task, method));
                Collection<TestEnricher> testEnrichers = serviceLoader.get().all(TestEnricher.class);
                for (TestEnricher enricher : testEnrichers) {
                    enricher.enrich(task);
                }
                enrichmentEvent.fire(new AfterEnrichment(task, method));
            } finally {
                containerContext.get().deactivate();
            }
        }
    }
}

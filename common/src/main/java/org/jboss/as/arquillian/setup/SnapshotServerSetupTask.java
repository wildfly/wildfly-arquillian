/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.arquillian.setup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.wildfly.plugin.tools.server.ServerManager;

/**
 * A setup task which takes a snapshot of the current configuration. It then invokes the
 * {@link #doSetup(ManagementClient, String)} which allows configuration of the running server. On
 * {@link #tearDown(ManagementClient, String)} the snapshot server configuration is used to reload the server and
 * overwrite the current configuration.
 * <p>
 * This setup tasks should be the first setup tasks if used with other setup tasks. Otherwise, the snapshot will have
 * changes from the previous setup tasks.
 * <p>
 * <p>
 * Note that if during the setup the server gets in a state of {@code reload-required}, then the after the
 * {@link #doSetup(ManagementClient, String)} is executed a reload will happen automatically.
 * </p>
 * <p>
 * If the {@link #doSetup(ManagementClient, String)} fails, the {@link #tearDown(ManagementClient, String)} method will
 * be invoked.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings({ "unused", "RedundantThrows" })
public class SnapshotServerSetupTask implements ServerSetupTask {
    private static final Logger LOGGER = Logger.getLogger(SnapshotServerSetupTask.class);

    private final Map<String, AutoCloseable> snapshots = new ConcurrentHashMap<>();

    @ArquillianResource
    private ServerManager serverManager;

    @Override
    public final void setup(final ManagementClient managementClient, final String containerId) throws Exception {
        try {
            final String fileName = serverManager.takeSnapshot();
            final AutoCloseable restorer = () -> {
                final ModelNode op = Operations.createOperation("reload");
                op.get("server-config").set(fileName);
                serverManager.executeReload(op);
                serverManager.waitFor(timeout(), TimeUnit.SECONDS);
                @SuppressWarnings("resource")
                final ModelNode result1 = serverManager.client().execute(Operations.createOperation("write-config"));
                if (!Operations.isSuccessfulOutcome(result1)) {
                    throw new RuntimeException(
                            "Failed to write config after restoring from snapshot " + Operations.getFailureDescription(result1)
                                    .asString());
                }
            };
            snapshots.put(containerId, restorer);
            try {
                doSetup(managementClient, containerId);
            } catch (Throwable e) {
                try {
                    restorer.close();
                } catch (Throwable t) {
                    LOGGER.warnf(t, "Failed to restore snapshot for %s: %s", getClass().getName(), fileName);
                }
                throw e;
            }
        } finally {
            serverManager.reloadIfRequired(timeout(), TimeUnit.SECONDS);
        }
    }

    @Override
    public final void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
        try {
            beforeRestore(managementClient, containerId);
        } finally {
            try {
                final AutoCloseable snapshot = snapshots.remove(containerId);
                if (snapshot != null) {
                    snapshot.close();
                }
            } finally {
                nonManagementCleanUp();
            }
        }
    }

    /**
     * Execute any necessary setup work that needs to happen before the first deployment to the given container.
     * <p>
     * If this method throws an exception, the {@link #tearDown(ManagementClient, String)} method will be invoked.
     * </p>
     *
     * @param managementClient management client to use to interact with the container
     * @param containerId      id of the container to which the deployment will be deployed
     *
     * @throws Exception if a failure occurs
     * @see #setup(ManagementClient, String)
     */
    protected void doSetup(final ManagementClient managementClient, final String containerId) throws Exception {
    }

    /**
     * Execute any necessary work required before the restore is completed. As an example removing a messaging queue
     * which triggers removing the queue from a remote server.
     *
     * @param managementClient management client to use to interact with the container
     * @param containerId      id of the container to which the deployment will be deployed
     *
     * @throws Exception if a failure occurs
     * @see #tearDown(ManagementClient, String)
     */
    protected void beforeRestore(final ManagementClient managementClient, final String containerId) throws Exception {
    }

    /**
     * Allows for cleaning up resources that may have been created during the setup. This is always executed even if the
     * {@link #doSetup(ManagementClient, String)} fails for some reason.
     *
     * @throws Exception if a failure occurs
     */
    protected void nonManagementCleanUp() throws Exception {
    }

    /**
     * The number seconds to wait for the server to reload after the server configuration has been restored or if a
     * reload was required in the {@link #doSetup(ManagementClient, String)}.
     *
     * @return the number of seconds to wait for a reload, the default is 10 seconds
     */
    protected long timeout() {
        return 10L;
    }
}

/*
 * Copyright 2016 Red Hat, Inc.
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

package org.wildfly.arquillian.domain;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.wildfly.arquillian.domain.api.DomainManager;
import org.wildfly.arquillian.domain.api.ServerDescription;

/**
 * Implements the domain manager operations.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public abstract class AbstractDomainManager implements DomainManager {
    private final String containerName;
    private final boolean lifecycleControlAllowed;

    protected AbstractDomainManager(final String containerName, final boolean lifecycleControlAllowed) {
        this.containerName = containerName;
        this.lifecycleControlAllowed = lifecycleControlAllowed;
    }

    @Override
    public Set<ServerDescription> getServers() {
        checkState(false);
        final Set<ServerDescription> servers = new LinkedHashSet<>();
        // Get all the servers in the servers
        final ModelNode op = Operations.createReadResourceOperation(Operations.createAddress(ClientConstants.HOST, "*", ClientConstants.SERVER_CONFIG));
        final ModelNode results = runtimeExecuteForSuccess(op);
        for (ModelNode result : results.asList()) {
            final ModelNode serverConfig = Operations.readResult(result);
            final String name = serverConfig.get(ClientConstants.NAME).asString();
            final String groupName = serverConfig.get(ClientConstants.GROUP).asString();
            final String hostName = Operations.getOperationAddress(result).asList().get(0).get(ClientConstants.HOST).asString();
            servers.add(new ServerDescription() {

                @Override
                public String getHostName() {
                    return hostName;
                }

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public String getGroupName() {
                    return groupName;
                }
            });
        }
        return Collections.unmodifiableSet(servers);
    }

    @Override
    public Set<ServerDescription> getServers(final String hostName) {
        checkState(false);
        final Set<ServerDescription> servers = new LinkedHashSet<>();
        // Get all the servers in the servers
        final ModelNode op = Operations.createReadResourceOperation(Operations.createAddress(ClientConstants.HOST, hostName, ClientConstants.SERVER_CONFIG));
        final ModelNode results = runtimeExecuteForSuccess(op);
        for (ModelNode result : results.asList()) {
            final ModelNode serverConfig = Operations.readResult(result);
            final String name = serverConfig.get(ClientConstants.NAME).asString();
            final String groupName = serverConfig.get(ClientConstants.GROUP).asString();
            servers.add(new ServerDescription() {

                @Override
                public String getHostName() {
                    return hostName;
                }

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public String getGroupName() {
                    return groupName;
                }
            });
        }
        return Collections.unmodifiableSet(servers);
    }

    @Override
    public Set<String> getServerGroups() {
        checkState(false);
        final Set<String> serverGroups = new LinkedHashSet<>();
        // Get all the hosts
        final ModelNode op = Operations.createOperation(ClientConstants.READ_CHILDREN_NAMES_OPERATION);
        op.get(ClientConstants.CHILD_TYPE).set(ClientConstants.SERVER_GROUP);
        final ModelNode result = runtimeExecuteForSuccess(op);
        for (ModelNode serverGroup : result.asList()) {
            serverGroups.add(serverGroup.asString());
        }
        return Collections.unmodifiableSet(serverGroups);
    }

    @Override
    public void startServer(final String hostName, final String serverName) {
        checkState(true);
        final ModelNode op = Operations.createOperation("start", Operations.createAddress(ClientConstants.HOST, hostName, ClientConstants.SERVER_CONFIG, serverName));
        op.get("blocking").set(true);
        runtimeExecuteForSuccess(op);
    }

    @Override
    public void stopServer(final String hostName, final String serverName) {
        checkState(true);
        final ModelNode op = Operations.createOperation("stop", Operations.createAddress(ClientConstants.HOST, hostName, ClientConstants.SERVER_CONFIG, serverName));
        op.get("blocking").set(true);
        runtimeExecuteForSuccess(op);
    }

    @Override
    public boolean isServerStarted(final String hostName, final String serverName) {
        checkState(true);
        final ModelNode op = Operations.createReadAttributeOperation(Operations.createAddress(ClientConstants.HOST, hostName, ClientConstants.SERVER_CONFIG, serverName), "status");
        try {
            final ModelNode result = getModelControllerClient().execute(op);
            if (Operations.isSuccessfulOutcome(result)) {
                return "STARTED".equals(Operations.readResult(result).asString());
            }
        } catch (IOException ignore) {

        }
        return false;
    }

    @Override
    public void restartServer(final String hostName, final String serverName) {
        checkState(true);
        final ModelNode op = Operations.createOperation("restart", Operations.createAddress(ClientConstants.HOST, hostName, ClientConstants.SERVER_CONFIG, serverName));
        op.get("blocking").set(true);
        runtimeExecuteForSuccess(op);
    }

    @Override
    public void resumeServer(final String hostName, final String serverName) {
        checkState(true);
        runtimeExecuteForSuccess(Operations.createOperation("resume", Operations.createAddress(ClientConstants.HOST, hostName, ClientConstants.SERVER_CONFIG, serverName)));
    }

    @Override
    public void suspendServer(final String hostName, final String serverName, final int timeout) {
        checkState(true);
        final ModelNode op = Operations.createOperation("suspend", Operations.createAddress(ClientConstants.HOST, hostName, ClientConstants.SERVER_CONFIG, serverName));
        op.get("timeout").set(timeout);
        runtimeExecuteForSuccess(op);
    }

    @Override
    public String getServerGroupName(final String hostName, final String serverName) {
        checkState(false);
        final ModelNode result = runtimeExecuteForSuccess(Operations.createReadAttributeOperation(Operations.createAddress(ClientConstants.HOST, hostName, ClientConstants.SERVER_CONFIG, serverName), "group"));
        return result.asString();
    }

    @Override
    public void reloadServers(final String name) {
        checkState(true);
        final ModelNode op = Operations.createOperation("reload-servers", Operations.createAddress(ClientConstants.SERVER_GROUP, name));
        op.get("blocking").set(true);
        runtimeExecuteForSuccess(op);
    }

    @Override
    public void restartServers(final String name) {
        checkState(true);
        final ModelNode op = Operations.createOperation("restart-servers", Operations.createAddress(ClientConstants.SERVER_GROUP, name));
        op.get("blocking").set(true);
        runtimeExecuteForSuccess(op);
    }

    @Override
    public void resumeServers(final String name) {
        checkState(true);
        runtimeExecuteForSuccess(Operations.createOperation("resume-servers", Operations.createAddress(ClientConstants.SERVER_GROUP, name)));
    }

    @Override
    public void startServers(final String name) {
        checkState(true);
        final ModelNode op = Operations.createOperation("start-servers", Operations.createAddress(ClientConstants.SERVER_GROUP, name));
        op.get("blocking").set(true);
        runtimeExecuteForSuccess(op);
    }

    @Override
    public void stopServers(final String name) {
        checkState(true);
        final ModelNode op = Operations.createOperation("stop-servers", Operations.createAddress(ClientConstants.SERVER_GROUP, name));
        op.get("blocking").set(true);
        runtimeExecuteForSuccess(op);
    }

    @Override
    public void suspendServers(final String name, final int timeout) {
        checkState(true);
        final ModelNode op = Operations.createOperation("suspend-servers", Operations.createAddress(ClientConstants.SERVER_GROUP, name));
        op.get("timeout").set(timeout);
        runtimeExecuteForSuccess(op);
    }

    /**
     * The client used to communicate with the server.
     *
     * @return the client
     */
    protected abstract ModelControllerClient getModelControllerClient();

    private void checkState(final boolean lifecycleControlRequired) {
        if (!isDomainStarted()) {
            throw new IllegalStateException("Container " + containerName + " has not been started.");
        }
        if (!lifecycleControlRequired && lifecycleControlAllowed) {
            throw new IllegalStateException("The lifecycle of container " + containerName + " is controlled by Arquillian. Cannot execute lifecycle operations.");
        }
    }

    private ModelNode runtimeExecuteForSuccess(final ModelNode op) {
        try {
            return executeForSuccess(op);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ModelNode executeForSuccess(final ModelNode op) throws IOException {
        final ModelControllerClient client = Objects.requireNonNull(getModelControllerClient(), "The client cannot be null");
        final ModelNode result = client.execute(op);
        if (!Operations.isSuccessfulOutcome(result)) {
            throw new RuntimeException(Operations.getFailureDescription(result).asString());
        }
        return Operations.readResult(result);
    }
}

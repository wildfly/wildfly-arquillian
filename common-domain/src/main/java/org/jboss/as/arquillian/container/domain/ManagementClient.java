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
package org.jboss.as.arquillian.container.domain;

import static org.jboss.as.controller.client.helpers.ClientConstants.AUTO_START;
import static org.jboss.as.controller.client.helpers.ClientConstants.DEPLOYMENT;
import static org.jboss.as.controller.client.helpers.ClientConstants.FAILURE_DESCRIPTION;
import static org.jboss.as.controller.client.helpers.ClientConstants.GROUP;
import static org.jboss.as.controller.client.helpers.ClientConstants.HOST;
import static org.jboss.as.controller.client.helpers.ClientConstants.INCLUDE_RUNTIME;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;
import static org.jboss.as.controller.client.helpers.ClientConstants.OUTCOME;
import static org.jboss.as.controller.client.helpers.ClientConstants.PROXIES;
import static org.jboss.as.controller.client.helpers.ClientConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.client.helpers.ClientConstants.RECURSIVE;
import static org.jboss.as.controller.client.helpers.ClientConstants.RECURSIVE_DEPTH;
import static org.jboss.as.controller.client.helpers.ClientConstants.RESULT;
import static org.jboss.as.controller.client.helpers.ClientConstants.SERVER;
import static org.jboss.as.controller.client.helpers.ClientConstants.SERVER_CONFIG;
import static org.jboss.as.controller.client.helpers.ClientConstants.SERVER_GROUP;
import static org.jboss.as.controller.client.helpers.ClientConstants.SOCKET_BINDING;
import static org.jboss.as.controller.client.helpers.ClientConstants.SOCKET_BINDING_GROUP;
import static org.jboss.as.controller.client.helpers.ClientConstants.STATUS;
import static org.jboss.as.controller.client.helpers.ClientConstants.SUBSYSTEM;
import static org.jboss.as.controller.client.helpers.ClientConstants.SUCCESS;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.as.arquillian.container.domain.Domain.Server;
import org.jboss.as.arquillian.container.domain.Domain.ServerGroup;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.DelegatingModelControllerClient;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.jboss.as.controller.client.helpers.domain.ServerIdentity;
import org.jboss.as.controller.client.helpers.domain.ServerStatus;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.wildfly.arquillian.domain.api.DomainManager;

/**
 * A helper class to join management related operations, like extract sub system ip/port (web/jmx) and deployment introspection.
 *
 * @author <a href="aslak@redhat.com">Aslak Knutsen</a>
 */
public class ManagementClient {

    private static final String SUBDEPLOYMENT = "subdeployment";
    private static final String UNDERTOW = "undertow";

    private static final String PROTOCOL_HTTP = "http";

    private static final String NAME = "name";
    private static final String SERVLET = "servlet";

    private static final String POSTFIX_WEB = ".war";
    private static final String POSTFIX_EAR = ".ear";

    private static final int ROOT_RECURSIVE_DEPTH = 3;

    private final DomainClient client;
    private final DomainClient userClient;
    private final Map<String, URI> subsystemURICache;
    private final DomainManager domainManager;

    // cache static RootNode
    private ModelNode rootNode = null;

    /**
     * Creates a new management client.
     *
     * @param client        the client to delegate management operations to
     * @param domainManager the domain manager
     */
    protected ManagementClient(final ModelControllerClient client, final DomainManager domainManager) {
        if (client == null) {
            throw new IllegalArgumentException("Client must be specified");
        }
        this.client = (client instanceof DomainClient ? ((DomainClient) client) : DomainClient.Factory.create(client));
        this.subsystemURICache = new HashMap<>();
        userClient = DomainClient.Factory.create(new NonClosingDomainClient(client));
        this.domainManager = domainManager;
    }

    /**
     * Creates a new management client.
     *
     * @param client         the client to delegate management operations to
     * @param mgmtAddress    not used
     * @param managementPort not used
     */
    public ManagementClient(final ModelControllerClient client, final String mgmtAddress, final int managementPort) {
        this(client, new ContainerDomainManager("UNKNOWN", false, client,  (client != null)));
    }

    // -------------------------------------------------------------------------------------||
    // Public API -------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    public DomainClient getControllerClient() {
        return userClient;
    }

    public Domain createDomain(Map<String, String> containerNameMap) {
        lazyLoadRootNode();

        Domain domain = new Domain();
        for (String hostNodeName : rootNode.get(HOST).keys()) {
            ModelNode serverConfigNode = rootNode.get(HOST).get(hostNodeName).get(SERVER_CONFIG);
            if (serverConfigNode.isDefined()) {
                for (String serverConfigName : serverConfigNode.keys()) {
                    ModelNode serverConfig = rootNode.get(HOST).get(hostNodeName).get(SERVER_CONFIG).get(serverConfigName);

                    Server server = new Server(
                            serverConfig.get(NAME).asString(),
                            hostNodeName,
                            serverConfig.get(GROUP).asString(),
                            serverConfig.get(AUTO_START).asBoolean());

                    if (containerNameMap.containsKey(server.getUniqueName())) {
                        server.setContainerName(containerNameMap.get(server.getUniqueName()));
                    }
                    domain.addServer(server);
                }
            }
        }
        for (String serverGroupName : rootNode.get(SERVER_GROUP).keys()) {

            ServerGroup group = new ServerGroup(serverGroupName);
            if(containerNameMap.containsKey(group.getName())) {
                group.setContainerName(containerNameMap.get(group.getName()));
            }
            domain.addServerGroup(group);
        }
        return domain;
    }

    public String getServerState(Domain.Server server) {
        lazyLoadRootNode();

        ModelNode hostNode = rootNode.get(HOST).get(server.getHost());
        if (!hostNode.isDefined()) {
            throw new IllegalArgumentException("Host not found on domain " + server.getHost());
        }

        ModelNode serverConfig = hostNode.get(SERVER_CONFIG).get(server.getName());
        if (!serverConfig.isDefined()) {
            throw new IllegalArgumentException("Server " + server + " not found on host " + server.getHost());
        }
        return serverConfig.get(STATUS).asString();
    }

    public HTTPContext getHTTPDeploymentMetaData(Server server, String uniqueDeploymentName) {

        URI webURI = getProtocolURI(server, PROTOCOL_HTTP);
        HTTPContext context = new HTTPContext(webURI.getHost(), webURI.getPort());
        try {
            ModelNode deploymentNode = readResource(createHostServerDeploymentAddress(
                    server.getHost(), server.getName(), uniqueDeploymentName), false); // don't include runtime information, workaround for bug in web statistics

            if (isWebArchive(uniqueDeploymentName)) {
                extractWebArchiveContexts(context, deploymentNode);
            } else if (isEnterpriseArchive(uniqueDeploymentName)) {
                extractEnterpriseArchiveContexts(context, deploymentNode);
            }

        } catch (Exception e) {
            throw new RuntimeException("Could not extract deployment information for server: " + server + " on deployment: "
                    + uniqueDeploymentName, e);
        }
        return context;
    }

    /**
     * Starts the servers in the server group.
     *
     * @param groupName the server group to start the servers for
     *
     * @throws IllegalStateException if the lifecycle is controlled by Arquillian or no container has been started
     */
    public void startServerGroup(String groupName) {
        domainManager.startServers(groupName);
    }

    /**
     * Stops the servers in the server group.
     *
     * @param groupName the server group to stop the servers for
     *
     * @throws IllegalStateException if the lifecycle is controlled by Arquillian or no container has been started
     */
    public void stopServerGroup(String groupName) {
        domainManager.stopServers(groupName);
    }

    /**
     * Starts the server on the host.
     *
     * @param server the server to start
     *
     * @throws IllegalStateException if the lifecycle is controlled by Arquillian or no container has been started
     */
    public void startServer(Server server) {
        domainManager.startServer(server.getHost(), server.getName());
    }

    /**
     * Stops the server on the host.
     *
     * @param server the server to stop
     *
     * @throws IllegalStateException if the lifecycle is controlled by Arquillian or no container has been started
     */
    public void stopServer(Server server) {
        domainManager.stopServer(server.getHost(), server.getName());
    }

    public boolean isServerStarted(Server server) {
        return domainManager.isServerStarted(server.getHost(), server.getName());
    }

    public boolean isDomainInRunningState() {
        final Map<ServerIdentity, ServerStatus> servers = new HashMap<>();
        try {
            final Map<ServerIdentity, ServerStatus> statuses = client.getServerStatuses();
            for (ServerIdentity id : statuses.keySet()) {
                final ServerStatus status = statuses.get(id);
                switch (status) {
                    case DISABLED:
                    case STARTED: {
                        servers.put(id, status);
                        break;
                    }
                }
            }
            return statuses.size() == servers.size();
        } catch (Exception e) {
            Logger.getLogger(ManagementClient.class).debug("Interrupted determining if domain is running", e);
        }
        return false;
    }

    public void close() {
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not close connection", e);
        }
    }

    // -------------------------------------------------------------------------------------||
    // Subsystem URI Lookup ---------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    private URI getProtocolURI(Server server, String subsystem) {
        String cacheKey = server + subsystem;
        URI subsystemURI = subsystemURICache.get(cacheKey);
        if (subsystemURI != null) {
            return subsystemURI;
        }
        subsystemURI = extractProtocolURI(server, subsystem);
        subsystemURICache.put(cacheKey, subsystemURI);
        return subsystemURI;
    }

    private URI extractProtocolURI(Server server, String protocol) {
        try {
            ModelNode node = readResource(createHostServerSocketBindingsAddress(server.getHost(), server.getName(),
                    getSocketBindingGroup(server.getGroup())));

            ModelNode socketBinding = node.get(SOCKET_BINDING).get(protocol);
            return URI.create(protocol + "://" + socketBinding.get("bound-address").asString() + ":"
                    + socketBinding.get("bound-port"));

        } catch (Exception e) {
            throw new RuntimeException("Could not extract address information from server: " + server + " for protocol "
                    + protocol + ". Is the server running?", e);
        }
    }

    private void readRootNode() throws Exception {
        rootNode = readResource(new ModelNode(), true, ROOT_RECURSIVE_DEPTH);
    }

    private String getSocketBindingGroup(String serverGroup) {
        lazyLoadRootNode();
        return rootNode.get(SERVER_GROUP).get(serverGroup).get(SOCKET_BINDING_GROUP).asString();
    }

    // -------------------------------------------------------------------------------------||
    // Metadata Extraction Operations -----------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    private boolean isEnterpriseArchive(String deploymentName) {
        return deploymentName.endsWith(POSTFIX_EAR);
    }

    private boolean isWebArchive(String deploymentName) {
        return deploymentName.endsWith(POSTFIX_WEB);
    }

    private ModelNode createHostServerDeploymentAddress(String host, String server, String deploymentName) {
        return new ModelNode().add(HOST, host).add(SERVER, server).add(DEPLOYMENT, deploymentName);
    }

    private ModelNode createHostServerSocketBindingsAddress(String host, String server, String socketBindingGroup) {
        return new ModelNode().add(HOST, host).add(SERVER, server).add(SOCKET_BINDING_GROUP, socketBindingGroup);
    }

    private void extractEnterpriseArchiveContexts(HTTPContext context, ModelNode deploymentNode) {
        if (deploymentNode.hasDefined(SUBDEPLOYMENT)) {
            for (ModelNode subdeployment : deploymentNode.get(SUBDEPLOYMENT).asList()) {
                String deploymentName = subdeployment.keys().iterator().next();
                if (isWebArchive(deploymentName)) {
                    extractWebArchiveContexts(context, deploymentName, subdeployment.get(deploymentName));
                }
            }
        }
    }

    private void extractWebArchiveContexts(HTTPContext context, ModelNode deploymentNode) {
        extractWebArchiveContexts(context, deploymentNode.get(NAME).asString(), deploymentNode);
    }

    private void extractWebArchiveContexts(HTTPContext context, String deploymentName, ModelNode deploymentNode) {
        if (deploymentNode.hasDefined(SUBSYSTEM)) {
            ModelNode subsystem = deploymentNode.get(SUBSYSTEM);
            if (subsystem.hasDefined(UNDERTOW)) {
                ModelNode webSubSystem = subsystem.get(UNDERTOW);
                if (webSubSystem.isDefined() && webSubSystem.hasDefined("context-root")) {
                    final String contextName = webSubSystem.get("context-root").asString();
                    if (webSubSystem.hasDefined(SERVLET)) {
                        for (final ModelNode servletNode : webSubSystem.get(SERVLET).asList()) {
                            for (final String servletName : servletNode.keys()) {
                                context.add(new Servlet(servletName, toContextName(contextName)));
                            }
                        }
                    }
                    /*
                     * This is a WebApp, it has some form of webcontext whether it has a Servlet or not. AS7 does not expose jsp
                     * / default servlet in mgm api
                     */
                    context.add(new Servlet("default", toContextName(contextName)));
                }
            }
        }
    }

    private String toContextName(String deploymentName) {
        String correctedName = deploymentName;
        if (correctedName.startsWith("/")) {
            correctedName = correctedName.substring(1);
        }
        if (correctedName.indexOf(".") != -1) {
            correctedName = correctedName.substring(0, correctedName.lastIndexOf("."));
        }
        return correctedName;
    }

    // -------------------------------------------------------------------------------------||
    // Common Management API Operations ---------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    private void lazyLoadRootNode() {
        try {
            if (rootNode == null) {
                readRootNode();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ModelNode readResource(ModelNode address) throws Exception {
        return readResource(address, true);
    }

    private ModelNode readResource(ModelNode address, boolean includeRuntime) throws Exception {
        return readResource(address, includeRuntime, null);
    }

    private ModelNode readResource(ModelNode address, boolean includeRuntime, Integer recursiveDepth) throws Exception {
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        if(recursiveDepth == null) {
            operation.get(RECURSIVE).set(true);
        }
        else {
            // To make it compatible with WFLY-3705 and pre-WFLY-3705 behavior
            // "recursive" is not set
            operation.get(RECURSIVE_DEPTH).set(recursiveDepth);
        }
        operation.get(INCLUDE_RUNTIME).set(includeRuntime);
        operation.get(PROXIES).set(true);
        operation.get(OP_ADDR).set(address);

        return executeForResult(operation);
    }

    private ModelNode executeForResult(final ModelNode operation) throws Exception {
        final ModelNode result = client.execute(operation);
        checkSuccessful(result, operation);
        return result.get(RESULT);
    }

    private void checkSuccessful(final ModelNode result, final ModelNode operation) throws UnSuccessfulOperationException {
        if (!SUCCESS.equals(result.get(OUTCOME).asString())) {
            throw new UnSuccessfulOperationException(result.get(FAILURE_DESCRIPTION).toString());
        }
    }

    private static class UnSuccessfulOperationException extends Exception {
        private static final long serialVersionUID = 1L;

        UnSuccessfulOperationException(String message) {
            super(message);
        }
    }

    private static class NonClosingDomainClient extends DelegatingModelControllerClient {

        NonClosingDomainClient(final ModelControllerClient delegate) {
            super(delegate);
        }

        @Override
        public void close() throws IOException {
            // Do nothing
        }
    }

}

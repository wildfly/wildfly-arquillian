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

import static org.jboss.as.controller.client.helpers.ClientConstants.CHILD_TYPE;
import static org.jboss.as.controller.client.helpers.ClientConstants.CONTROLLER_PROCESS_STATE_STARTING;
import static org.jboss.as.controller.client.helpers.ClientConstants.CONTROLLER_PROCESS_STATE_STOPPING;
import static org.jboss.as.controller.client.helpers.ClientConstants.DEPLOYMENT;
import static org.jboss.as.controller.client.helpers.ClientConstants.FAILURE_DESCRIPTION;
import static org.jboss.as.controller.client.helpers.ClientConstants.INCLUDE_RUNTIME;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP;
import static org.jboss.as.controller.client.helpers.ClientConstants.OP_ADDR;
import static org.jboss.as.controller.client.helpers.ClientConstants.OUTCOME;
import static org.jboss.as.controller.client.helpers.ClientConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.client.helpers.ClientConstants.READ_CHILDREN_NAMES_OPERATION;
import static org.jboss.as.controller.client.helpers.ClientConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.client.helpers.ClientConstants.RECURSIVE_DEPTH;
import static org.jboss.as.controller.client.helpers.ClientConstants.RESULT;
import static org.jboss.as.controller.client.helpers.ClientConstants.SUBSYSTEM;
import static org.jboss.as.controller.client.helpers.ClientConstants.SUCCESS;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServerConnection;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.callback.CallbackHandler;

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.JMXContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;
import org.jboss.logging.Logger;

/**
 * A helper class to join management related operations, like extract sub system ip/port (web/jmx)
 * and deployment introspection.
 *
 * <p>
 * Instances of this type are not thread-safe.
 * </p>
 *
 * @author <a href="aslak@redhat.com">Aslak Knutsen</a>
 */
public class ManagementClient implements Closeable {

    private static final Logger logger = Logger.getLogger(ManagementClient.class);

    private static final String SUBDEPLOYMENT = "subdeployment";

    private static final String UNDERTOW = "undertow";
    private static final String REST = "jaxrs";
    private static final String NAME = "name";
    private static final String SERVLET = "servlet";

    private static final String POSTFIX_WEB = ".war";
    private static final String POSTFIX_EAR = ".ear";
    private static final ModelNode UNDERTOW_SUBSYSTEM_ADDRESS = new ModelNode().add("subsystem", UNDERTOW);
    private static final String REST_APPLICATION_PATH = "ArquillianRESTRunnerEE9";

    private final String mgmtAddress;
    private final int mgmtPort;
    private final String mgmtProtocol;
    private final ModelControllerClient client;
    private final CommonContainerConfiguration config;

    private boolean initialized = false;
    private URI webUri;
    private URI ejbUri;

    private ModelNode undertowSubsystem = null;

    private MBeanServerConnection connection;
    private JMXConnector connector;
    private boolean undertowSubsystemPresent = false;
    private boolean jmxSubsystemPresent = false;
    private boolean closed = false;

    public ManagementClient(ModelControllerClient client, final String mgmtAddress, final int managementPort,
            final String protocol) {
        this.client = Objects.requireNonNull(client, "Client must not be null");
        this.mgmtAddress = mgmtAddress;
        this.mgmtPort = managementPort;
        this.mgmtProtocol = protocol;
        this.config = null;
    }

    public ManagementClient(ModelControllerClient client, final CommonContainerConfiguration config) {
        this.client = Objects.requireNonNull(client, "Client must not be null");
        this.mgmtAddress = config.getManagementAddress();
        this.mgmtPort = config.getManagementPort();
        this.mgmtProtocol = config.getManagementProtocol();
        this.config = config;
    }

    // -------------------------------------------------------------------------------------||
    // Public API -------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    /**
     * Returns the client used to connect to the server.
     *
     * @return the client
     *
     * @throws IllegalStateException if this has been {@linkplain #close() closed}
     */
    public ModelControllerClient getControllerClient() {
        checkState();
        return client;
    }

    /**
     * Resets the client. {@linkplain #close() Closes} open resources and resets flags so that the client itself can be
     * reinitialized later if desired.
     */
    void reset() {
        close();
        initialized = false;
        closed = false;
    }

    /**
     * Checks whether or not the Undertow subsystem is present and sets the internal state if it is. An invocation of
     * this should happen after the server has been started.
     *
     * @throws IllegalStateException if this has been {@linkplain #close() closed}
     */
    private void init() {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                checkState();

                if (!initialized) {
                    initialized = true;
                    try {
                        ModelNode op = Operations.createReadResourceOperation(UNDERTOW_SUBSYSTEM_ADDRESS, true);
                        ModelNode result = client.execute(op);
                        undertowSubsystemPresent = Operations.isSuccessfulOutcome(result);
                        if (undertowSubsystemPresent) {
                            undertowSubsystem = Operations.readResult(result);
                        }
                        URI webUri;
                        try {
                            webUri = new URI("http://localhost:8080");
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }
                        if (config != null && config.getSocketBindingName() != null) {
                            webUri = getBinding(config.getSocketBindingName());
                        } else {
                            if (undertowSubsystem != null && undertowSubsystem.hasDefined("server")) {
                                List<Property> vhosts = undertowSubsystem.get("server").asPropertyList();
                                ModelNode socketBinding = new ModelNode();
                                if (!vhosts.isEmpty()) {// if empty no virtual hosts defined
                                    socketBinding = vhosts.get(0)
                                            .getValue()
                                            .get("http-listener", "default")
                                            .get("socket-binding");
                                }
                                if (socketBinding.isDefined()) {
                                    webUri = getBinding(socketBinding.asString());
                                }
                            }
                        }
                        ManagementClient.this.webUri = webUri;
                        try {
                            ejbUri = new URI("http-remoting", webUri.getUserInfo(), webUri.getHost(), webUri.getPort(), null,
                                    null, null);
                        } catch (URISyntaxException e) {
                            throw new RuntimeException(e);
                        }

                        // Determine if JMX is available
                        op = Operations.createOperation(READ_CHILDREN_NAMES_OPERATION);
                        op.get(CHILD_TYPE).set(SUBSYSTEM);
                        result = client.execute(op);
                        if (!Operations.isSuccessfulOutcome(result)) {
                            throw new RuntimeException("Failed to determine if the JMX subsystem is present: "
                                    + Operations.getFailureDescription(result).asString());
                        }
                        jmxSubsystemPresent = Operations.readResult(result)
                                .asList()
                                .stream()
                                .map(ModelNode::asString)
                                .anyMatch("jmx"::equals);
                    } catch (Exception e) {
                        throw new RuntimeException("Could not init arquillian protocol", e);
                    }
                }
                return null;
            }
        });
    }

    /**
     * @return The base URI or the web susbsystem. Usually http://localhost:8080
     *
     * @throws IllegalStateException if this has been {@linkplain #close() closed}
     */
    public URI getWebUri() {
        init();
        return webUri;
    }

    /**
     * Gets the meta-data.
     *
     * @return the meta-data
     *
     * @throws IllegalStateException if this has been {@linkplain #close() closed}
     */
    public ProtocolMetaData getProtocolMetaData(String deploymentName) {
        init();
        ProtocolMetaData metaData = new ProtocolMetaData();
        if (jmxSubsystemPresent) {
            metaData.addContext(new JMXContext(getConnection()));
        }
        if (undertowSubsystemPresent) {
            URI webURI = getWebUri();
            HTTPContext context = new HTTPContext(webURI.getHost(), webURI.getPort(),
                    "https".equalsIgnoreCase(webURI.getScheme()));
            metaData.addContext(context);

            try {
                for (Servlet servlet : resolveContexts(deploymentName)) {
                    context.add(servlet);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return metaData;
    }

    /**
     * Checks whether or not the server is running.
     * <p>
     * Note that if this client has been {@linkplain #close() closed} the state of the server cannot be checked.
     * </p>
     *
     * @return {@code true} if the server is running, otherwise {@code false}
     *
     * @throws IllegalStateException if this has been {@linkplain #close() closed}
     */
    public boolean isServerInRunningState() {
        checkState();
        try {
            ModelNode op = new ModelNode();
            op.get(OP).set(READ_ATTRIBUTE_OPERATION);
            op.get(OP_ADDR).setEmptyList();
            op.get(NAME).set("server-state");

            ModelNode rsp = client.execute(op);
            return SUCCESS.equals(rsp.get(OUTCOME).asString())
                    && !CONTROLLER_PROCESS_STATE_STARTING.equals(rsp.get(RESULT).asString())
                    && !CONTROLLER_PROCESS_STATE_STOPPING.equals(rsp.get(RESULT).asString());
        } catch (RuntimeException rte) {
            throw rte;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Checks whether or not the client has been closed.
     *
     * @return {@code true} if the client has been closed otherwise {@code false}
     */
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (!closed) {
            try {
                client.close();
                closed = true;
            } catch (IOException e) {
                throw new RuntimeException("Could not close connection", e);
            } finally {
                if (connector != null) {
                    try {
                        connector.close();
                    } catch (IOException e) {
                        throw new RuntimeException("Could not close JMX connection", e);
                    }
                }
            }
        }
    }

    private static ModelNode defined(final ModelNode node, final String message) {
        if (!node.isDefined()) {
            throw new IllegalStateException(message);
        }
        return node;
    }

    private URI getBinding(final String socketBinding) {
        String protocol = "http";
        String host = null;
        int port = -1;
        if (config != null) {
            host = config.getHost();
            port = config.getPort();
            protocol = config.getProtocol();
        }
        try {
            if (host == null || port < 0) {
                ModelNode sbgOp = new ModelNode();
                sbgOp.get(OP).set(READ_CHILDREN_NAMES_OPERATION);
                sbgOp.get(CHILD_TYPE).set("socket-binding-group");
                final ModelNode socketBindingGroups = executeForResult(sbgOp);
                final String socketBindingGroupName = socketBindingGroups.asList().get(0).asString();
                final ModelNode operation = new ModelNode();
                operation.get(OP_ADDR).get("socket-binding-group").set(socketBindingGroupName);
                operation.get(OP_ADDR).get("socket-binding").set(socketBinding);
                operation.get(OP).set(READ_RESOURCE_OPERATION);
                operation.get("include-runtime").set(true);
                ModelNode binding = executeForResult(operation);
                if (host == null) {
                    host = formatIP(binding.get("bound-address").asString());
                }

                // Check if we need to look up the port
                if (port < 0) {
                    port = defined(binding.get("bound-port"),
                            socketBindingGroupName + " -> " + socketBinding + " -> bound-port is undefined").asInt();
                }
            }
            return URI.create(protocol + "://" + NetworkUtils.formatPossibleIpv6Address(host) + ":" + port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String formatIP(String ip) {
        // it appears some system can return a binding with the zone specifier on the end
        if (ip.contains(":") && ip.contains("%")) {
            ip = ip.split("%")[0];
        }
        if (ip.equals("0.0.0.0")) {
            logger.debug("WildFly is bound to 0.0.0.0 which is correct, setting client to 127.0.0.1");
            ip = "127.0.0.1";
        }
        return ip;
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

    private Collection<Servlet> resolveContexts(final String deploymentName) throws IOException {
        final Collection<Servlet> contexts = new ArrayList<>();
        if (isWebArchive(deploymentName)) {
            contexts.addAll(resolveServletContexts(readDeploymentNode(deploymentName, null)));
        } else if (isEnterpriseArchive(deploymentName)) {
            contexts.addAll(resolveServletContexts(readDeploymentNode(deploymentName, findWebDeployments(deploymentName))));
        }
        return contexts;
    }

    private Set<String> findWebDeployments(final String deploymentName) throws IOException {
        final ModelNode op = Operations.createOperation(ClientConstants.READ_CHILDREN_NAMES_OPERATION,
                Operations.createAddress("deployment", deploymentName));
        op.get(ClientConstants.CHILD_TYPE).set("subdeployment");
        final ModelNode result = client.execute(op);
        if (!Operations.isSuccessfulOutcome(result)) {
            throw new RuntimeException(String.format("Could not find sudeployments for %s: %s", deploymentName,
                    Operations.getFailureDescription(result).asString()));
        }
        return Operations.readResult(result)
                .asList()
                .stream()
                .map(ModelNode::asString)
                .filter(this::isWebArchive)
                .collect(Collectors.toSet());
    }

    private List<ModelNode> readDeploymentNode(final String deploymentName, final Iterable<String> subDeploymentNames)
            throws IOException {
        if (subDeploymentNames == null) {
            ModelNode operation = Operations
                    .createReadResourceOperation(Operations.createAddress(DEPLOYMENT, deploymentName, SUBSYSTEM));
            operation.get(RECURSIVE_DEPTH).set(2);
            operation.get(INCLUDE_RUNTIME).set(true);

            // Check the Undertow subsystem
            final ModelNode result = client.execute(operation);
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new RuntimeException(Operations.getFailureDescription(result).asString());
            }
            return Collections.singletonList(parseResult(result));
        }
        final List<ModelNode> deployments = new ArrayList<>();
        final Operations.CompositeOperationBuilder builder = Operations.CompositeOperationBuilder.create();
        for (String subDeploymentName : subDeploymentNames) {
            ModelNode operation = Operations.createReadResourceOperation(
                    Operations.createAddress(DEPLOYMENT, deploymentName, SUBDEPLOYMENT, subDeploymentName, SUBSYSTEM));
            operation.get(RECURSIVE_DEPTH).set(2);
            operation.get(INCLUDE_RUNTIME).set(true);
            builder.addStep(operation);
        }
        // Check the Undertow subsystem
        final ModelNode result = client.execute(builder.build());
        if (!Operations.isSuccessfulOutcome(result)) {
            throw new RuntimeException(Operations.getFailureDescription(result).asString());
        }
        // Read each step
        for (Property property : Operations.readResult(result).asPropertyList()) {
            deployments.add(parseResult(property.getValue()));
        }
        return deployments;
    }

    private static ModelNode parseResult(final ModelNode result) {
        final ModelNode model = new ModelNode();
        // Find the undertow and REST subsystem
        for (ModelNode subsystemResult : Operations.readResult(result).asList()) {
            final ModelNode a = Operations.getOperationAddress(subsystemResult);
            for (Property property : a.asPropertyList()) {
                if (SUBSYSTEM.equals(property.getName())) {
                    if (UNDERTOW.equals(property.getValue().asString())) {
                        model.get(UNDERTOW).set(Operations.readResult(subsystemResult));
                    } else if (REST.equals(property.getValue().asString())) {
                        model.get(REST).set(Operations.readResult(subsystemResult));
                    }
                }
            }
        }
        return model;
    }

    private static Collection<Servlet> resolveServletContexts(final Iterable<ModelNode> deployments) {
        final Collection<Servlet> contexts = new ArrayList<>();
        for (ModelNode deployment : deployments) {
            if (!deployment.isDefined()) {
                continue;
            }
            String contextName = null;
            if (deployment.hasDefined(UNDERTOW)) {
                final ModelNode undertow = deployment.get(UNDERTOW);
                if (undertow.hasDefined("context-root")) {
                    contextName = toContextName(undertow.get("context-root").asString());
                    if (undertow.hasDefined(SERVLET)) {
                        for (final ModelNode servletNode : undertow.get(SERVLET).asList()) {
                            for (final String servletName : servletNode.keys()) {
                                contexts.add(new Servlet(servletName, contextName));
                            }
                        }
                    }
                    /*
                     * This is a WebApp, it has some form of webcontext whether it has a
                     * Servlet or not. WildFly does not expose jsp / default servlet in mgm api
                     */
                    contexts.add(new Servlet("default", contextName));
                }
            }
            if (deployment.hasDefined(REST)) {
                final ModelNode rest = deployment.get(REST);
                if (rest.isDefined() && rest.hasDefined("rest-resource")) {
                    for (Property restResource : rest.get("rest-resource").asPropertyList()) {
                        // Register the REST protocol context if found
                        findRestServlet(restResource.getValue()
                                .get("rest-resource-paths"), contextName).ifPresent(contexts::add);
                    }
                }
            }
        }
        return contexts;
    }

    private static String toContextName(String deploymentName) {
        if (deploymentName.isEmpty() || deploymentName.charAt(0) != '/') {
            return deploymentName;
        }
        return deploymentName.substring(1);
    }

    private static Optional<Servlet> findRestServlet(final ModelNode resourcePaths, final String servletContext) {
        if (resourcePaths.isDefined() && resourcePaths.getType() == ModelType.LIST) {
            for (ModelNode current : resourcePaths.asList()) {
                // Check if we contain the supported REST context
                if (current.hasDefined("resource-methods")) {
                    for (ModelNode resourceMethod : current.get("resource-methods").asList()) {
                        final String rawValue = resourceMethod.asString();
                        // Check if this is the context for the REST protocol, if so add it and
                        if (rawValue.contains(REST_APPLICATION_PATH)) {
                            return Optional.of(new Servlet(REST_APPLICATION_PATH, toContextName(servletContext)));
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    // -------------------------------------------------------------------------------------||
    // Common Management API Operations ---------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    private ModelNode executeForResult(final ModelNode operation) throws Exception {
        checkState();
        final ModelNode result = client.execute(operation);
        checkSuccessful(result, operation);
        return result.get(RESULT);
    }

    private void checkSuccessful(final ModelNode result,
            final ModelNode operation) throws UnSuccessfulOperationException {
        if (!SUCCESS.equals(result.get(OUTCOME).asString())) {
            logger.error("Operation " + operation + " did not succeed. Result was " + result);
            throw new UnSuccessfulOperationException(result.get(
                    FAILURE_DESCRIPTION).toString());
        }
    }

    private MBeanServerConnection getConnection() {
        MBeanServerConnection connection = this.connection;
        if (connection == null) {
            try {
                final Map<String, Object> env = new HashMap<>();
                if (Authentication.username != null && !Authentication.username.isEmpty()) {
                    // Only set this is there is a username as it disabled local authentication.
                    env.put(CallbackHandler.class.getName(), Authentication.getCallbackHandler());
                }
                if (config.getAuthenticationConfig() != null) {
                    env.put("wildfly.config.url", config.getAuthenticationConfig());
                }
                final JMXServiceURL serviceURL = getRemoteJMXURL();
                final JMXConnector connector = this.connector = JMXConnectorFactory.connect(serviceURL, env);
                connection = this.connection = new MBeanConnectionProxy(connector.getMBeanServerConnection());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return connection;
    }

    public JMXServiceURL getRemoteJMXURL() {
        try {
            if ("http-remoting".equals(mgmtProtocol) || "remote+http".equals(mgmtProtocol)) {
                return new JMXServiceURL(
                        "service:jmx:remote+http://" + NetworkUtils.formatPossibleIpv6Address(mgmtAddress) + ":" + mgmtPort);
            } else if (mgmtProtocol.equals("https-remoting")) {
                return new JMXServiceURL(
                        "service:jmx:remote+https://" + NetworkUtils.formatPossibleIpv6Address(mgmtAddress) + ":" + mgmtPort);
            } else {
                return new JMXServiceURL(
                        "service:jmx:remoting-jmx://" + NetworkUtils.formatPossibleIpv6Address(mgmtAddress) + ":" + mgmtPort);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create JMXServiceURL:" + this, e);
        }
    }

    public int getMgmtPort() {
        return mgmtPort;
    }

    public String getMgmtAddress() {
        return NetworkUtils.formatPossibleIpv6Address(mgmtAddress);
    }

    public String getMgmtProtocol() {
        return mgmtProtocol;
    }

    /**
     * Returns the URI for EJB's.
     *
     * @return the resolved EJB URI
     *
     * @throws IllegalStateException if this has been {@linkplain #close() closed}
     */
    public URI getRemoteEjbURL() {
        init();
        return ejbUri;
    }

    private void checkState() {
        if (closed) {
            throw new IllegalStateException("The client connection has been closed.");
        }
    }

    // -------------------------------------------------------------------------------------||
    // Helper classes ---------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||
    private static class UnSuccessfulOperationException extends Exception {
        private static final long serialVersionUID = 1L;

        UnSuccessfulOperationException(String message) {
            super(message);
        }
    }

    private class MBeanConnectionProxy implements MBeanServerConnection {
        private MBeanServerConnection connection;

        /**
         * @param connection connection to delegate to
         */
        private MBeanConnectionProxy(MBeanServerConnection connection) {
            super();
            this.connection = connection;
        }

        @Override
        public ObjectInstance createMBean(String className, ObjectName name) throws ReflectionException,
                InstanceAlreadyExistsException, MBeanException, NotCompliantMBeanException,
                IOException {
            checkConnection();
            return connection.createMBean(className, name);
        }

        @Override
        public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName) throws ReflectionException,
                InstanceAlreadyExistsException, MBeanException, NotCompliantMBeanException,
                InstanceNotFoundException, IOException {
            checkConnection();
            return connection.createMBean(className, name, loaderName);
        }

        @Override
        public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
                throws ReflectionException, InstanceAlreadyExistsException, MBeanException,
                NotCompliantMBeanException, IOException {
            checkConnection();
            return connection.createMBean(className, name, params, signature);
        }

        @Override
        public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params,
                String[] signature) throws ReflectionException, InstanceAlreadyExistsException,
                MBeanException, NotCompliantMBeanException, InstanceNotFoundException, IOException {
            checkConnection();
            return connection.createMBean(className, name, loaderName, params, signature);
        }

        @Override
        public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException, IOException {
            checkConnection();
            connection.unregisterMBean(name);
        }

        @Override
        public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException, IOException {
            try {
                return connection.getObjectInstance(name);
            } catch (IOException e) {
                checkConnection();
                return connection.getObjectInstance(name);
            }
        }

        @Override
        public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) throws IOException {
            try {
                return connection.queryMBeans(name, query);
            } catch (IOException e) {
                checkConnection();
                return connection.queryMBeans(name, query);
            }
        }

        @Override
        public Set<ObjectName> queryNames(ObjectName name, QueryExp query) throws IOException {
            try {
                return connection.queryNames(name, query);
            } catch (IOException e) {
                checkConnection();
                return connection.queryNames(name, query);
            }
        }

        @Override
        public boolean isRegistered(ObjectName name) throws IOException {
            try {
                return connection.isRegistered(name);
            } catch (IOException e) {
                checkConnection();
                return connection.isRegistered(name);
            }
        }

        @Override
        public Integer getMBeanCount() throws IOException {
            try {
                return connection.getMBeanCount();
            } catch (IOException e) {
                checkConnection();
                return connection.getMBeanCount();
            }
        }

        @Override
        public Object getAttribute(ObjectName name, String attribute) throws MBeanException, AttributeNotFoundException,
                InstanceNotFoundException, ReflectionException, IOException {
            try {
                return connection.getAttribute(name, attribute);
            } catch (IOException e) {
                checkConnection();
                return connection.getAttribute(name, attribute);
            }
        }

        @Override
        public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException,
                ReflectionException, IOException {
            try {
                return connection.getAttributes(name, attributes);
            } catch (IOException e) {
                checkConnection();
                return connection.getAttributes(name, attributes);
            }
        }

        @Override
        public void setAttribute(ObjectName name, Attribute attribute) throws InstanceNotFoundException,
                AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException, IOException {
            checkConnection();
            connection.setAttribute(name, attribute);
        }

        @Override
        public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException,
                ReflectionException, IOException {
            checkConnection();
            return connection.setAttributes(name, attributes);
        }

        @Override
        public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
                throws InstanceNotFoundException, MBeanException, ReflectionException, IOException {
            checkConnection();
            return connection.invoke(name, operationName, params, signature);
        }

        @Override
        public String getDefaultDomain() throws IOException {
            try {
                return connection.getDefaultDomain();
            } catch (IOException e) {
                checkConnection();
                return connection.getDefaultDomain();
            }
        }

        @Override
        public String[] getDomains() throws IOException {
            try {
                return connection.getDomains();
            } catch (IOException e) {
                checkConnection();
                return connection.getDomains();
            }
        }

        @Override
        public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter,
                Object handback) throws InstanceNotFoundException, IOException {
            try {
                connection.addNotificationListener(name, listener, filter, handback);
            } catch (IOException e) {
                if (!checkConnection()) {
                    connection.addNotificationListener(name, listener, filter, handback);
                } else {
                    throw e;
                }
            }
        }

        @Override
        public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
                throws InstanceNotFoundException, IOException {
            try {
                connection.addNotificationListener(name, listener, filter, handback);
            } catch (IOException e) {
                if (!checkConnection()) {
                    connection.addNotificationListener(name, listener, filter, handback);
                } else {
                    throw e;
                }
            }
        }

        @Override
        public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException,
                ListenerNotFoundException, IOException {
            try {
                connection.removeNotificationListener(name, listener);
            } catch (IOException e) {
                if (!checkConnection()) {
                    connection.removeNotificationListener(name, listener);
                } else {
                    throw e;
                }
            }
        }

        @Override
        public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
                throws InstanceNotFoundException, ListenerNotFoundException, IOException {
            try {
                connection.removeNotificationListener(name, listener, filter, handback);
            } catch (IOException e) {
                if (!checkConnection()) {
                    connection.removeNotificationListener(name, listener, filter, handback);
                } else {
                    throw e;
                }
            }
        }

        @Override
        public void removeNotificationListener(ObjectName name, NotificationListener listener)
                throws InstanceNotFoundException, ListenerNotFoundException, IOException {
            try {
                connection.removeNotificationListener(name, listener);
            } catch (IOException e) {
                if (!checkConnection()) {
                    connection.removeNotificationListener(name, listener);
                } else {
                    throw e;
                }
            }
        }

        @Override
        public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter,
                Object handback) throws InstanceNotFoundException, ListenerNotFoundException, IOException {
            try {
                connection.removeNotificationListener(name, listener, filter, handback);
            } catch (IOException e) {
                if (!checkConnection()) {
                    connection.removeNotificationListener(name, listener, filter, handback);
                } else {
                    throw e;
                }
            }
        }

        @Override
        public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException,
                ReflectionException, IOException {
            try {
                return connection.getMBeanInfo(name);
            } catch (IOException e) {
                checkConnection();
                return connection.getMBeanInfo(name);
            }
        }

        @Override
        public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException, IOException {
            try {
                return connection.isInstanceOf(name, className);
            } catch (IOException e) {
                checkConnection();
                return connection.isInstanceOf(name, className);
            }
        }

        private boolean checkConnection() {
            try {
                this.connection.getDefaultDomain();
                return true;
            } catch (IOException ioe) {
                logger.debug("JMX connection error.", ioe);
            }
            this.connection = reconnect();
            return false;
        }

        private MBeanServerConnection reconnect() {
            try {
                final Map<String, Object> env = new HashMap<>();
                env.put(CallbackHandler.class.getName(), Authentication.getCallbackHandler());
                final JMXConnector connector = ManagementClient.this.connector = JMXConnectorFactory.connect(getRemoteJMXURL(),
                        env);
                connection = connector.getMBeanServerConnection();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return connection;
        }
    }
}

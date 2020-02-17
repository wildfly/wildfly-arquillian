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

import static org.jboss.as.arquillian.container.Authentication.getCallbackHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.ModelControllerClientConfiguration;
import org.jboss.as.controller.client.helpers.DelegatingModelControllerClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * A JBossAS deployable container
 *
 * @author Thomas.Diesler@jboss.com
 * @since 17-Nov-2010
 */
public abstract class CommonDeployableContainer<T extends CommonContainerConfiguration> implements DeployableContainer<T> {

    private static final String JBOSS_URL_PKG_PREFIX = "org.jboss.ejb.client.naming";

    private T containerConfig;

    @Inject
    @ContainerScoped
    private InstanceProducer<ManagementClient> managementClient;

    @Inject
    @ContainerScoped
    private InstanceProducer<ArchiveDeployer> archiveDeployer;

    @Inject
    @ApplicationScoped
    private InstanceProducer<Context> jndiContext;

    private final StandaloneDelegateProvider mccProvider = new StandaloneDelegateProvider();
    private ContainerDescription containerDescription = null;
    private URI authenticationConfig = null;

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 3.0");
    }

    @Override
    public void setup(T config) {
        containerConfig = config;
        final String authenticationConfig = containerConfig.getAuthenticationConfig();

        // Check for an Elytron configuration
        if (authenticationConfig != null) {
            this.authenticationConfig = URI.create(authenticationConfig);
        }

        final ManagementClient client = new ManagementClient(new DelegatingModelControllerClient(mccProvider), containerConfig);
        managementClient.set(client);

        archiveDeployer.set(new ArchiveDeployer(client));
    }

    @Override
    public final void start() throws LifecycleException {
        // Create a client configuration builder from the container configuration
        final ModelControllerClientConfiguration.Builder clientConfigBuilder = new ModelControllerClientConfiguration.Builder()
                .setProtocol(containerConfig.getManagementProtocol())
                .setHostName(containerConfig.getManagementAddress())
                .setPort(containerConfig.getManagementPort())
                .setAuthenticationConfigUri(authenticationConfig);

        // only "copy" the timeout if one was set.
        final int connectionTimeout = containerConfig.getConnectionTimeout();
        if(connectionTimeout > 0) {
            clientConfigBuilder.setConnectionTimeout(connectionTimeout);
        }

        // Check for username and password authentication
        if(containerConfig.getUsername() != null) {
            Authentication.username = containerConfig.getUsername();
            Authentication.password = containerConfig.getPassword();
            clientConfigBuilder.setHandler(getCallbackHandler());
        }
        mccProvider.setDelegate(ModelControllerClient.Factory.create(clientConfigBuilder.build()));

        try {
            final Properties jndiProps = new Properties();
            jndiProps.setProperty(Context.URL_PKG_PREFIXES, JBOSS_URL_PKG_PREFIX);
            jndiContext.set(new InitialContext(jndiProps));
        } catch (final NamingException ne) {
            throw new LifecycleException("Could not set JNDI Naming Context", ne);
        }

        try {
            startInternal();
        } catch (LifecycleException e) {
            safeCloseClient();
            throw e;
        }
    }

    protected abstract void startInternal() throws LifecycleException;

    @Override
    public final void stop() throws LifecycleException {
        try {
            stopInternal(null);
        } finally {
            safeCloseClient();
        }
    }

    public final void stop(Integer timeout) throws LifecycleException {
        try {
            stopInternal(timeout);
        } finally {
            safeCloseClient();
        }
    }

    protected abstract void stopInternal(Integer timeout) throws LifecycleException;

    /**
     * Returns a description for the running container. If the container has not been started {@code null} will be
     * returned.
     *
     * @return the description for the running container or {@code null} if the container has not yet been started
     */
    public ContainerDescription getContainerDescription() {
        if (containerDescription == null) {
            try {
                final ManagementClient client = getManagementClient();
                // The management client should be set when the container is started
                if (client == null) return null;
                containerDescription = StandardContainerDescription.lookup(client);
            } catch (IOException e) {
                Logger.getLogger(getClass()).warn("Failed to lookup the container description.", e);
                containerDescription = StandardContainerDescription.NULL_DESCRIPTION;
            }
        }
        return containerDescription;
    }

    protected T getContainerConfiguration() {
        return containerConfig;
    }

    protected ManagementClient getManagementClient() {
        return managementClient.get();
    }

    protected ModelControllerClient getModelControllerClient() {
        return getManagementClient().getControllerClient();
    }

    /**
     * Checks to see if the attribute is a valid attribute for the operation. This is useful to determine if the running
     * container supports an attribute for the version running.
     *
     * <p>
     * This is the same as executing {@link #isOperationAttributeSupported(ModelNode, String, String) isOperationAttriubuteSupported(null, operationName, attributeName)}
     * </p>
     *
     * @param operationName the operation name
     * @param attributeName the attribute name
     *
     * @return {@code true} if the attribute is supported or {@code false} if the attribute was not found on the
     * operation description
     *
     * @throws IOException           if an error occurs while attempting to execute the operation
     * @throws IllegalStateException if the operation fails
     */
    protected boolean isOperationAttributeSupported(final String operationName, final String attributeName) throws IOException {
        return isOperationAttributeSupported(null, operationName, attributeName);
    }

    /**
     * Checks to see if the attribute is a valid attribute for the operation. This is useful to determine if the running
     * container supports an attribute for the version running.
     *
     * @param address       the address or {@code null} for the root resource
     * @param operationName the operation name
     * @param attributeName the attribute name
     *
     * @return {@code true} if the attribute is supported or {@code false} if the attribute was not found on the
     * operation description
     *
     * @throws IOException           if an error occurs while attempting to execute the operation
     * @throws IllegalStateException if the operation fails
     */
    protected boolean isOperationAttributeSupported(final ModelNode address, final String operationName, final String attributeName) throws IOException {
        final ModelControllerClient client = getModelControllerClient();
        final ModelNode op;
        if (address == null) {
            op = Operations.createOperation(ModelDescriptionConstants.READ_OPERATION_DESCRIPTION_OPERATION);
        } else {
            op = Operations.createOperation(ModelDescriptionConstants.READ_OPERATION_DESCRIPTION_OPERATION, address);
        }
        op.get(ModelDescriptionConstants.NAME).set(operationName);
        final ModelNode result = client.execute(op);
        if (Operations.isSuccessfulOutcome(result)) {
            final ModelNode params = Operations.readResult(result).get(ModelDescriptionConstants.REQUEST_PROPERTIES);
            return params.keys().contains(attributeName);
        }
        final String msg;
        if (address == null) {
            msg = String.format("Failed to determine if attribute %s is supported for operation %s. %s", attributeName, operationName, Operations.getFailureDescription(result));
        } else {
            msg = String.format("Failed to determine if attribute %s is supported for operation %s:%s. %s", attributeName, addressToCliString(address), operationName, Operations.getFailureDescription(result));
        }
        throw new IllegalStateException(msg);
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        String runtimeName = archiveDeployer.get().deploy(archive);
        return getManagementClient().getProtocolMetaData(runtimeName);
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        archiveDeployer.get().undeploy(archive.getName());
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("not implemented");
    }

    private void safeCloseClient() {
        try {
            // Reset the client, this should close the internal resources and setup reinitialization
            ManagementClient client = getManagementClient();
            if (client != null) {
                client.reset();
            }
        } catch (final Exception e) {
            Logger.getLogger(getClass()).warn("Caught exception closing ManagementClient", e);
        } finally {
            mccProvider.setDelegate(null);
        }
    }

    private static String addressToCliString(final ModelNode address) {
        if (address == null) {
            return "";
        }
        final StringBuilder result = new StringBuilder(32);
        for (Property property : address.asPropertyList()) {
            result.append('/').append(property.getName()).append('=').append(property.getValue().asString());
        }
        return result.toString();
    }

    private static class StandaloneDelegateProvider implements DelegatingModelControllerClient.DelegateProvider {
        private final AtomicReference<ModelControllerClient> delegate;

        private StandaloneDelegateProvider() {
            this.delegate = new AtomicReference<>();
        }

        void setDelegate(final ModelControllerClient client) {
            delegate.set(client);
        }

        @Override
        public ModelControllerClient getDelegate() {
            final ModelControllerClient result = delegate.get();
            if (result == null) {
                throw new IllegalStateException("The container has not been started. The client is not usable.");
            }
            return result;
        }
    }
}

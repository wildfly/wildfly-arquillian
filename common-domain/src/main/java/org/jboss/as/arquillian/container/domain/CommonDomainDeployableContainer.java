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

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.config.descriptor.impl.ContainerDefImpl;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.Container.State;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.spi.event.SetupContainer;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.jboss.as.arquillian.container.domain.Domain.Server;
import org.jboss.as.arquillian.container.domain.Domain.ServerGroup;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.ModelControllerClientConfiguration;
import org.jboss.as.controller.client.helpers.DelegatingModelControllerClient;
import org.jboss.as.controller.client.helpers.domain.DomainClient;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.wildfly.arquillian.domain.ServerGroupArchive;
import org.wildfly.arquillian.domain.api.DomainManager;

/**
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public abstract class CommonDomainDeployableContainer<T extends CommonDomainContainerConfiguration> implements
        DeployableContainer<T> {

    @Inject
    private Instance<ContainerRegistry> containerRegistryInstance;

    @Inject
    private Instance<ServiceLoader> serviceLoaderInstance;

    @Inject
    private Instance<Injector> injectorInst;

    @Inject
    private Instance<Container> containerInst;

    @Inject // we need to fire setup events to trigger the container context creation
    private Event<SetupContainer> setupEvent;

    private final Logger log = Logger.getLogger(CommonDomainDeployableContainer.class.getName());
    private T containerConfig;
    private ManagementClient managementClient;
    private volatile ContainerDomainManager domainManager;

    @Inject
    @ContainerScoped
    private InstanceProducer<ArchiveDeployer> archiveDeployerInst;

    @Inject
    @ContainerScoped
    private InstanceProducer<ManagementClient> managementClientInst;

    @Inject
    @ContainerScoped
    private InstanceProducer<Domain> domainInst;

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 3.0");
    }

    @Override
    public void setup(T config) {
        containerConfig = config;

        // Register on setup so these can be injected into manual mode client tests
        final DomainClient domainClient = DomainClient.Factory.create(new DelegatingModelControllerClient(DomainDelegateProvider.INSTANCE));
        domainManager = new ContainerDomainManager(getContainerName(), isControllable(), domainClient);
        managementClient = new ManagementClient(domainClient, domainManager);
        managementClientInst.set(managementClient);

        ArchiveDeployer archiveDeployer = new ArchiveDeployer(managementClient);
        archiveDeployerInst.set(archiveDeployer);
    }

    @Override
    public void start() throws LifecycleException {
        // Create a client configuration builder from the container configuration
        final ModelControllerClientConfiguration.Builder clientConfigBuilder = new ModelControllerClientConfiguration.Builder()
                .setHostName(containerConfig.getManagementHostName())
                .setPort(containerConfig.getManagementPort());

        if (containerConfig.getUsername() != null) {
            Authentication.username = containerConfig.getUsername();
            Authentication.password = containerConfig.getPassword();
            clientConfigBuilder.setHandler(Authentication.getCallbackHandler());
        }

        final String authenticationConfig = containerConfig.getAuthenticationConfig();

        // Check for an Elytron configuration
        if (authenticationConfig != null) {
            clientConfigBuilder.setAuthenticationConfigUri(URI.create(authenticationConfig));
        }

        // Configure the current client and set the delegate for the provider so the same management client can be used
        // during starts and stops
        DomainDelegateProvider.INSTANCE.setDelegate(ModelControllerClient.Factory.create(clientConfigBuilder.build()));

        try {
            startInternal();
        } catch (LifecycleException e) {
            safeCloseClient();
            throw e;
        }

        ContainerRegistry registry = containerRegistryInstance.get();

        Map<String, String> containerNameMap = containerConfig.getContainerNameMap();
        Map<String, String> modeMap = containerConfig.getContainerModeMap();

        Domain domain = managementClient.createDomain(containerNameMap);
        domainInst.set(domain);

        // Register all ServerGroups
        for (ServerGroup serverGroup: domain.getServerGroups()) {
            Container serverContainer = createServerGroupContainer(registry, archiveDeployerInst.get(), domain, serverGroup);
            String mode = mapMode(modeMap, serverContainer.getName());
            if(mode != null) {
                serverContainer.getContainerConfiguration().setMode(mode);
            }
            setupEvent.fire(new SetupContainer(serverContainer));
            serverContainer.setState(Container.State.STARTED);
        }

        // Register all Servers
        for (Server server : domain.getServers()) {
            Container serverContainer = createServerContainer(registry, server);
            String mode = mapMode(modeMap, serverContainer.getName());
            if(mode != null) {
                serverContainer.getContainerConfiguration().setMode(mode);
            }
            String serverStatus = managementClient.getServerState(server);

            setupEvent.fire(new SetupContainer(serverContainer));
            if (serverStatus.equals("STARTED")) {
                serverContainer.setState(Container.State.STARTED);
            } else {
                serverContainer.setState(Container.State.STOPPED);
            }
        }
        domainManager.setContainerStarted(true);
    }

    @Override
    public final void stop() throws LifecycleException {
        domainManager.setContainerStarted(false);
        try {
            updateDomainMembersState(State.STOPPED);
            stopInternal();
        } finally {
            safeCloseClient();
        }
    }

    protected abstract void startInternal() throws LifecycleException;

    protected abstract void stopInternal() throws LifecycleException;

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        // Get all the server groups we're deploying to
        final Set<String> serverGroups = getServerGroups(archive);
        if (!serverGroups.isEmpty()) {
            final ProtocolMetaData metaData = new ProtocolMetaData();
            final ArchiveDeployer deployer = archiveDeployerInst.get();
            final String uniqueName = deployer.deploy(archive, serverGroups);
            final Domain domain = domainInst.get();
            for (String serverGroupName : serverGroups) {
                for (Server server : domain.getServersInGroup(serverGroupName)) {
                    metaData.addContext(new LazyHttpContext(server, uniqueName, managementClient));
                }
            }
            return metaData;
        }
        throw new DeploymentException("Could not determine the server-group to deploy the archive to.");
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        // Get all the server groups we're deploying to
        final Set<String> serverGroups = getServerGroups(archive);
        if (!serverGroups.isEmpty()) {
            final ArchiveDeployer deployer = archiveDeployerInst.get();
            deployer.undeploy(archive.getName(), serverGroups);
        } else {
            throw new DeploymentException("Could not determine the server-group for the undeploy.");
        }
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Can not deploy directly from a Domain Controller");
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Can not undeploy directly from a Domain Controller");
    }

    /**
     * Returns the domain manager used for this container.
     *
     * <p>
     * Do note this may return {@code null} if {@link #setup(CommonDomainContainerConfiguration)} has not been invoked.
     * </p>
     *
     * @return the domain manager used
     */
    public DomainManager getDomainManager() {
        return domainManager;
    }

    protected T getContainerConfiguration() {
        return containerConfig;
    }

    protected ManagementClient getManagementClient() {
        return managementClient;
    }

    protected ModelControllerClient getModelControllerClient() {
        return managementClient.getControllerClient();
    }

    private void safeCloseClient() {
        try {
            managementClient.close();
        } catch (Exception e) {
            log.log(Level.WARNING, "Caught exception closing ModelControllerClient", e);
        } finally {
            DomainDelegateProvider.INSTANCE.setDelegate(null);
        }
    }

    private Container createServerContainer(ContainerRegistry registry, final Server server) {
        ContainerDef def = new ContainerDefImpl("arquillian")
                                .container(server.getContainerName())
                                .setMode(getContainerMode());

        return registry.create(def, new ServiceLoader() {

            @Override
            public <X> X onlyOne(Class<X> serviceClass, Class<? extends X> defaultServiceClass) {
                return serviceLoaderInstance.get().onlyOne(serviceClass, defaultServiceClass);
            }

            @Override
            public <X> X onlyOne(Class<X> serviceClass) {
                if (serviceClass == DeployableContainer.class) {
                    return serviceClass.cast(injectorInst.get().inject(new ServerContainer(domainManager, server)));
                }
                return serviceLoaderInstance.get().onlyOne(serviceClass);
            }

            @Override
            public <X> Collection<X> all(Class<X> serviceClass) {
                return serviceLoaderInstance.get().all(serviceClass);
            }
        });
    }

    private Container createServerGroupContainer(ContainerRegistry registry, final ArchiveDeployer archiveDeployer,
            final Domain domain, final ServerGroup serverGroup) {
        ContainerDef def = new ContainerDefImpl("arquillian")
                                .container(serverGroup.getContainerName())
                                .setMode(getContainerMode());

        return registry.create(def, new ServiceLoader() {

            @Override
            public <X> X onlyOne(Class<X> serviceClass, Class<? extends X> defaultServiceClass) {
                return serviceLoaderInstance.get().onlyOne(serviceClass, defaultServiceClass);
            }

            @Override
            public <X> X onlyOne(Class<X> serviceClass) {
                if (serviceClass == DeployableContainer.class) {
                    return serviceClass.cast(injectorInst.get().inject(
                            new ServerGroupContainer(managementClient, archiveDeployer, domain, serverGroup, domainManager)));
                }
                return serviceLoaderInstance.get().onlyOne(serviceClass);
            }

            @Override
            public <X> Collection<X> all(Class<X> serviceClass) {
                return serviceLoaderInstance.get().all(serviceClass);
            }
        });
    }

    /**
     * Update all Arquillian Containers in the Domain (group and server) with the new State.
     *
     * The Domain Controller can start/stop nodes outside of Arquillian's control.
     */
    private void updateDomainMembersState(State newState) {
        ContainerRegistry registry = containerRegistryInstance.get();
        Domain domain = domainInst.get();

        for (Server server : domain.getServers()) {
            registry.getContainer(server.getContainerName()).setState(newState);
        }

        for (ServerGroup group : domain.getServerGroups()) {
            registry.getContainer(group.getContainerName()).setState(newState);
        }
    }

    private String mapMode(Map<String, String> modeMap, String name) {
        for(Map.Entry<String, String> entry : modeMap.entrySet()) {
            if(name.matches(entry.getKey())) {
                log.info("Mapping " + name + " to container mode " + entry.getValue() + " based on expression " + entry.getKey());
                return entry.getValue();
            }
        }
        return null;
    }

    private String getContainerMode() {
        final Container container = containerInst.get();
        return container.getContainerConfiguration().getMode();
    }

    private String getContainerName() {
        final Container container = containerInst.get();
        return container.getName();
    }

    private boolean isControllable() {
        final String mode = getContainerMode();
        return "manual".equalsIgnoreCase(mode) || "custom".equalsIgnoreCase(mode);
    }

    private static Set<String> getServerGroups(final Archive<?> archive) throws DeploymentException {
        if (archive instanceof ServerGroupArchive) {
            return ((ServerGroupArchive<?>) archive).getServerGroups();
        }
        throw new DeploymentException("Could not determine the server-group to deploy the archive to.");
    }

    private static class DomainDelegateProvider implements DelegatingModelControllerClient.DelegateProvider {
        static final DomainDelegateProvider INSTANCE = new DomainDelegateProvider();
        private final AtomicReference<ModelControllerClient> delegate;

        private DomainDelegateProvider() {
            this.delegate = new AtomicReference<>();
        }

        void setDelegate(final ModelControllerClient client) {
            delegate.set(client);
        }

        @Override
        public ModelControllerClient getDelegate() {
            final ModelControllerClient result = delegate.get();
            if (result == null) {
                throw new IllegalStateException("The client has been closed. Ensure the container has been started.");
            }
            return result;
        }
    }
}
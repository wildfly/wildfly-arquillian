/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.domain;

import java.util.Set;

import org.jboss.arquillian.container.spi.Container.State;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.as.arquillian.container.domain.Domain.Server;
import org.jboss.as.arquillian.container.domain.Domain.ServerGroup;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.wildfly.arquillian.domain.api.DomainManager;

/**
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServerGroupContainer implements DeployableContainer<EmptyConfiguration> {

    @Inject
    @ContainerScoped
    private InstanceProducer<ArchiveDeployer> archiveDeployerInst;

    @Inject
    @ContainerScoped
    private InstanceProducer<ModelControllerClient> clientInst;

    @Inject
    private Instance<ContainerRegistry> containerRegistryInst;

    private ManagementClient client;
    private ArchiveDeployer deployer;
    private ServerGroup serverGroup;
    private Domain domain;
    private final DomainManager domainManager;

    public ServerGroupContainer(ManagementClient client, ArchiveDeployer deployer, Domain domain, ServerGroup serverGroup,
            final DomainManager domainManager) {
        this.client = client;
        this.deployer = deployer;
        this.domain = domain;
        this.serverGroup = serverGroup;
        this.domainManager = domainManager;
    }

    @Override
    public Class<EmptyConfiguration> getConfigurationClass() {
        return EmptyConfiguration.class;
    }

    @Override
    public void setup(EmptyConfiguration configuration) {
        archiveDeployerInst.set(deployer);
        clientInst.set(client.getControllerClient());
    }

    @Override
    public void start() throws LifecycleException {
        if (domainManager.isDomainStarted()) {
            domainManager.startServers(serverGroup.getName());
        }
        updateGroupMembersContainerState(State.STARTED);
    }

    @Override
    public void stop() throws LifecycleException {
        if (domainManager.isDomainStarted()) {
            domainManager.stopServers(serverGroup.getName());
        }
        updateGroupMembersContainerState(State.STOPPED);
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 5.0");
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        String uniqueName = deployer.deploy(archive, serverGroup.getName());

        ProtocolMetaData metaData = new ProtocolMetaData();
        for (Server server : domain.getServersInGroup(serverGroup)) {
            metaData.addContext(new LazyHttpContext(server, uniqueName, client));
        }
        return metaData;
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        deployer.undeploy(archive.getName(), serverGroup.getName());
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException();
    }

    /**
     * Update all Arquillian Containers in the Group with the new State.
     *
     * The Group can start/stop nodes outside of Arquillian's control.
     */
    private void updateGroupMembersContainerState(State newState) {
        Set<Server> servers = domain.getServersInGroup(serverGroup);

        ContainerRegistry registry = containerRegistryInst.get();

        for (Server server : servers) {
            registry.getContainer(server.getContainerName()).setState(newState);
        }
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.domain;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.as.arquillian.container.domain.Domain.Server;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.arquillian.domain.api.DomainManager;

/**
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ServerContainer implements DeployableContainer<EmptyConfiguration> {

    private final DomainManager domainManager;
    private final Server server;

    public ServerContainer(final DomainManager domainManager, Server server) {
        this.domainManager = domainManager;
        this.server = server;
    }

    @Override
    public Class<EmptyConfiguration> getConfigurationClass() {
        return EmptyConfiguration.class;
    }

    @Override
    public void setup(EmptyConfiguration configuration) {
    }

    @Override
    public void start() throws LifecycleException {
        if (domainManager.isDomainStarted()) {
            domainManager.startServer(server.getHost(), server.getName());
        }
    }

    @Override
    public void stop() throws LifecycleException {
        if (domainManager.isDomainStarted()) {
            domainManager.stopServer(server.getHost(), server.getName());
        }
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {
        return new ProtocolDescription("Servlet 5.0");
    }

    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        throw new UnsupportedOperationException(
                "Can not deploy to a single server in the domain, target server-group " + server.getGroup());
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        throw new UnsupportedOperationException(
                "Can not undeploy from a single server in the domain, target server-group " + server.getGroup());
    }
}

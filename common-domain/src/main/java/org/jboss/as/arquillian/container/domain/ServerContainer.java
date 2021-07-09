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

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.as.arquillian.container.domain.Domain.Server;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
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
        throw new UnsupportedOperationException("Can not deploy to a single server in the domain, target server-group " + server.getGroup());
    }

    @Override
    public void undeploy(Archive<?> archive) throws DeploymentException {
        throw new UnsupportedOperationException("Can not undeploy from a single server in the domain, target server-group " + server.getGroup());
    }

    @Override
    public void deploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Can not deploy to a single server in the domain, target server-group " + server.getGroup());
    }

    @Override
    public void undeploy(Descriptor descriptor) throws DeploymentException {
        throw new UnsupportedOperationException("Can not undeploy from a single server in the domain, target server-group " + server.getGroup());
    }
}

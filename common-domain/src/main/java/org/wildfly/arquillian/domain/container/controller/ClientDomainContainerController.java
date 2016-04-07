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

package org.wildfly.arquillian.domain.container.controller;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.impl.client.container.ClientContainerController;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.as.arquillian.container.domain.CommonDomainDeployableContainer;
import org.wildfly.arquillian.domain.api.DomainContainerController;
import org.wildfly.arquillian.domain.api.DomainManager;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ClientDomainContainerController extends ClientContainerController implements DomainContainerController {


    @Inject
    private Instance<ContainerRegistry> containerRegistry;

    @Override
    public void reloadServers(final String containerQualifier, final String groupName) {
        getDomainController(containerQualifier, true).reloadServers(groupName);
    }

    @Override
    public void restartServers(final String containerQualifier, final String groupName) {
        getDomainController(containerQualifier, true).restartServers(groupName);
    }

    @Override
    public void resumeServers(final String containerQualifier, final String groupName) {
        getDomainController(containerQualifier, true).resumeServers(groupName);
    }

    @Override
    public void startServers(final String containerQualifier, final String groupName) {
        getDomainController(containerQualifier, true).startServers(groupName);
    }

    @Override
    public void stopServers(final String containerQualifier, final String groupName) {
        getDomainController(containerQualifier, true).stopServers(groupName);
    }

    @Override
    public void suspendServers(final String containerQualifier, final String groupName, final int timeout) {
        getDomainController(containerQualifier, true).suspendServers(groupName, timeout);
    }

    @Override
    public void startServer(final String containerQualifier, final String hostName, final String serverName) {
        getDomainController(containerQualifier, true).startServer(hostName, serverName);
    }

    @Override
    public void stopServer(final String containerQualifier, final String hostName, final String serverName) {
        getDomainController(containerQualifier, true).stopServer(hostName, serverName);
    }

    @Override
    public boolean isServerStarted(final String containerQualifier, final String hostName, final String serverName) {
        return getDomainController(containerQualifier, false).isServerStarted(hostName, serverName);
    }

    @Override
    public void restartServer(final String containerQualifier, final String hostName, final String serverName) {
        getDomainController(containerQualifier, true).restartServer(hostName, serverName);
    }

    @Override
    public void resumeServer(final String containerQualifier, final String hostName, final String serverName) {
        getDomainController(containerQualifier, true).resumeServer(hostName, serverName);
    }

    @Override
    public void suspendServer(final String containerQualifier, final String hostName, final String serverName, final int timeout) {
        getDomainController(containerQualifier, true).suspendServer(hostName, serverName, timeout);
    }

    private DomainManager getDomainController(final String containerQualifier, boolean requiresControllable) {
        final ContainerRegistry registry = containerRegistry.get();
        if (registry == null) {
            throw new IllegalArgumentException("No container registry in context");
        }

        if (!containerExists(registry.getContainers(), containerQualifier)) {
            throw new IllegalArgumentException("No container with the specified name exists");
        }
        if (requiresControllable && !isControllableContainer(registry.getContainers(), containerQualifier)) {
            throw new IllegalArgumentException("Could not stop " + containerQualifier + " container. The container life cycle is controlled by Arquillian");
        }

        if (!isStarted(containerQualifier)) {
            throw new IllegalArgumentException(String.format("Container %s has not been started.", containerQualifier));
        }

        final Container container = registry.getContainer(containerQualifier);
        final DeployableContainer<?> deployableContainer = container.getDeployableContainer();
        if (deployableContainer instanceof CommonDomainDeployableContainer) {
            return CommonDomainDeployableContainer.class.cast(deployableContainer).getDomainManager();
        }
        throw new IllegalArgumentException(String.format("The container defined with %s is not a domain controller", containerQualifier));
    }
}

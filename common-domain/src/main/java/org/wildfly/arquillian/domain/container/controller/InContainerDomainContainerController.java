/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.domain.container.controller;

import org.jboss.arquillian.container.test.impl.client.container.ContainerContainerController;
import org.wildfly.arquillian.domain.api.DomainContainerController;
import org.wildfly.arquillian.domain.container.controller.command.GetServerStatusCommand;
import org.wildfly.arquillian.domain.container.controller.command.Lifecycle;
import org.wildfly.arquillian.domain.container.controller.command.ServerGroupLifecycleCommand;
import org.wildfly.arquillian.domain.container.controller.command.ServerLifecycleCommand;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class InContainerDomainContainerController extends ContainerContainerController implements DomainContainerController {

    @Override
    public void reloadServers(final String containerQualifier, final String groupName) {
        getCommandService().execute(new ServerGroupLifecycleCommand(containerQualifier, Lifecycle.RELOAD, groupName));
    }

    @Override
    public void restartServers(final String containerQualifier, final String groupName) {
        getCommandService().execute(new ServerGroupLifecycleCommand(containerQualifier, Lifecycle.RESTART, groupName));
    }

    @Override
    public void resumeServers(final String containerQualifier, final String groupName) {
        getCommandService().execute(new ServerGroupLifecycleCommand(containerQualifier, Lifecycle.RESUME, groupName));
    }

    @Override
    public void startServers(final String containerQualifier, final String groupName) {
        getCommandService().execute(new ServerGroupLifecycleCommand(containerQualifier, Lifecycle.START, groupName));
    }

    @Override
    public void stopServers(final String containerQualifier, final String groupName) {
        getCommandService().execute(new ServerGroupLifecycleCommand(containerQualifier, Lifecycle.STOP, groupName));
    }

    @Override
    public void suspendServers(final String containerQualifier, final String groupName, final int timeout) {
        getCommandService().execute(new ServerGroupLifecycleCommand(containerQualifier, Lifecycle.SUSPEND, groupName, timeout));
    }

    @Override
    public void startServer(final String containerQualifier, final String hostName, final String serverName) {
        getCommandService().execute(new ServerLifecycleCommand(containerQualifier, Lifecycle.START, hostName, serverName));
    }

    @Override
    public void stopServer(final String containerQualifier, final String hostName, final String serverName) {
        getCommandService().execute(new ServerLifecycleCommand(containerQualifier, Lifecycle.STOP, hostName, serverName));
    }

    @Override
    public boolean isServerStarted(final String containerQualifier, final String hostName, final String serverName) {
        return getCommandService().execute(new GetServerStatusCommand(containerQualifier, hostName, serverName));
    }

    @Override
    public void restartServer(final String containerQualifier, final String hostName, final String serverName) {
        getCommandService().execute(new ServerLifecycleCommand(containerQualifier, Lifecycle.RESTART, hostName, serverName));
    }

    @Override
    public void resumeServer(final String containerQualifier, final String hostName, final String serverName) {
        getCommandService().execute(new ServerLifecycleCommand(containerQualifier, Lifecycle.RESUME, hostName, serverName));
    }

    @Override
    public void suspendServer(final String containerQualifier, final String hostName, final String serverName,
            final int timeout) {
        getCommandService()
                .execute(new ServerLifecycleCommand(containerQualifier, Lifecycle.SUSPEND, hostName, serverName, timeout));
    }
}

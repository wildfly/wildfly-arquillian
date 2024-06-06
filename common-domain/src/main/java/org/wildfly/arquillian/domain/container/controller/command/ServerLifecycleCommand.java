/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.domain.container.controller.command;

import org.jboss.arquillian.container.test.impl.client.deployment.command.AbstractCommand;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ServerLifecycleCommand extends AbstractCommand<Boolean> {

    private final String containerQualifier;
    private final Lifecycle lifecycle;
    private final String hostName;
    private final String serverName;
    private final int timeout;

    public ServerLifecycleCommand(final String containerQualifier, final Lifecycle lifecycle, final String hostName,
            final String serverName) {
        this(containerQualifier, lifecycle, hostName, serverName, -1);
    }

    public ServerLifecycleCommand(final String containerQualifier, final Lifecycle lifecycle, final String hostName,
            final String serverName, final int timeout) {
        this.containerQualifier = containerQualifier;
        this.lifecycle = lifecycle;
        this.hostName = hostName;
        this.serverName = serverName;
        this.timeout = timeout;
    }

    public String getContainerQualifier() {
        return containerQualifier;
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public String getHostName() {
        return hostName;
    }

    public String getServerName() {
        return serverName;
    }

    public int getTimeout() {
        return timeout;
    }
}

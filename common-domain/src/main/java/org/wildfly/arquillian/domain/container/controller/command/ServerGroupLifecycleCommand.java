/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.domain.container.controller.command;

import org.jboss.arquillian.container.test.impl.client.deployment.command.AbstractCommand;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ServerGroupLifecycleCommand extends AbstractCommand<Boolean> {

    private final String containerQualifier;
    private final Lifecycle lifecycle;
    private final String serverGroupName;
    private final int timeout;

    public ServerGroupLifecycleCommand(final String containerQualifier, final Lifecycle lifecycle,
            final String serverGroupName) {
        this(containerQualifier, lifecycle, serverGroupName, -1);
    }

    public ServerGroupLifecycleCommand(final String containerQualifier, final Lifecycle lifecycle, final String serverGroupName,
            final int timeout) {
        this.containerQualifier = containerQualifier;
        this.lifecycle = lifecycle;
        this.serverGroupName = serverGroupName;
        this.timeout = timeout;
    }

    public String getContainerQualifier() {
        return containerQualifier;
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public String getServerGroupName() {
        return serverGroupName;
    }

    public int getTimeout() {
        return timeout;
    }
}

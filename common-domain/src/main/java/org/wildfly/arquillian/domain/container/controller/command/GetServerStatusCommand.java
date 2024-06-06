/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.domain.container.controller.command;

import org.jboss.arquillian.container.test.impl.client.deployment.command.AbstractCommand;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class GetServerStatusCommand extends AbstractCommand<Boolean> {

    private final String containerQualifier;
    private final String hostName;
    private final String serverName;

    public GetServerStatusCommand(final String containerQualifier, final String hostName, final String serverName) {
        this.containerQualifier = containerQualifier;
        this.hostName = hostName;
        this.serverName = serverName;
    }

    public String getContainerQualifier() {
        return containerQualifier;
    }

    public String getHostName() {
        return hostName;
    }

    public String getServerName() {
        return serverName;
    }
}

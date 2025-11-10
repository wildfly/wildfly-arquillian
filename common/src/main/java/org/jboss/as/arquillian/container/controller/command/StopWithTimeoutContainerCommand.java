/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.controller.command;

import org.jboss.arquillian.container.test.impl.client.deployment.command.AbstractCommand;

/**
 * {@link StopWithTimeoutContainerCommand} fired by
 * {@link org.jboss.as.arquillian.container.controller.InContainerWildFlyContainerController}.
 *
 * @author Radoslav Husar
 */
public class StopWithTimeoutContainerCommand extends AbstractCommand<String> {
    private static final long serialVersionUID = 8652099525721931094L;

    private final String containerQualifier;
    private final int suspendTimeout;

    public StopWithTimeoutContainerCommand(String containerQualifier, int suspendTimeout) {
        this.containerQualifier = containerQualifier;
        this.suspendTimeout = suspendTimeout;
    }

    public String getContainerQualifier() {
        return containerQualifier;
    }

    public int getTimeout() {
        return suspendTimeout;
    }
}
/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.controller.command;

import org.jboss.arquillian.container.test.impl.client.deployment.command.AbstractCommand;

/**
 * StopWithTimeoutContainerCommand coming from
 * {@link org.jboss.as.arquillian.container.controller.InContainerWildFlyContainerController}.
 *
 * @author Radoslav Husar
 * @version Jan 2015
 */
public class StopWithTimeoutContainerCommand extends AbstractCommand<String> {
    private static final long serialVersionUID = 1L;

    private final String containerQualifier;
    private final int timeout;

    public StopWithTimeoutContainerCommand(String containerQualifier, int timeout) {
        this.containerQualifier = containerQualifier;
        this.timeout = timeout;
    }

    public String getContainerQualifier() {
        return containerQualifier;
    }

    public int getTimeout() {
        return timeout;
    }
}
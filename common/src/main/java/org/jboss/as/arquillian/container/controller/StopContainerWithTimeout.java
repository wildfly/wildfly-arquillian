/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.controller;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.event.ContainerControlEvent;

/**
 * {@link ContainerControlEvent} implementation fired in {@link ClientWildFlyContainerController#stop(java.lang.String, int)}.
 *
 * @author Radoslav Husar
 * @version Jan 2015
 */
public class StopContainerWithTimeout extends ContainerControlEvent {

    private final int timeout;

    /**
     * @param container container to stop
     * @param timeout   graceful shutdown timeout in seconds
     */
    public StopContainerWithTimeout(Container container, int timeout) {
        super(container);
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }
}

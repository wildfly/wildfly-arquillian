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
 */
public class StopContainerWithTimeout extends ContainerControlEvent {

    private final int suspendTimeout;

    /**
     * @param container      container to stop
     * @param suspendTimeout graceful shutdown suspend timeout in seconds
     */
    public StopContainerWithTimeout(Container container, int suspendTimeout) {
        super(container);
        this.suspendTimeout = suspendTimeout;
    }

    public int getTimeout() {
        return suspendTimeout;
    }
}

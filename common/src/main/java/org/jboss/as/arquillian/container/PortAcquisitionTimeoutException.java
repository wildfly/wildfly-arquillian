/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container;

import org.jboss.arquillian.container.spi.client.container.LifecycleException;

/**
 * Denotes that a port could not be obtained within a designated timeout period.
 *
 * @author <a href="mailto:alr@jboss.org">ALR</a>
 * @see {@link https://issues.jboss.org/browse/AS7-4070}
 */
public class PortAcquisitionTimeoutException extends LifecycleException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance noting the port that could not be acquired in the designated amount of time
     *
     * @param port
     * @param timeoutSeconds
     */
    public PortAcquisitionTimeoutException(final int port, final int timeoutSeconds) {
        super("Could not acquire requested port " + port + " in " + timeoutSeconds + " seconds");
    }
}

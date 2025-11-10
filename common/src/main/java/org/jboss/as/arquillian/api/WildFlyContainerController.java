/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.api;

import org.jboss.arquillian.container.test.api.ContainerController;

/**
 * {@inheritDoc}
 * <p/>
 * This extension of the generic controller provides WildFly-specific lifecycle control methods.
 *
 * @author Radoslav Husar
 */
public interface WildFlyContainerController extends ContainerController {

    /**
     * Stops the given container with a specified suspend timeout corresponding to {@code :shutdown(suspend-timeout=...)}
     * management operation.
     * <p>
     * Note that if the {@code stopTimeoutInSeconds} configuration property is set at a lower value than the suspendTimeout
     * parameter,
     * the container process may be destroyed before the controlled shutdown finishes.
     * </p>
     *
     * @param containerQualifier container qualifier
     * @param suspendTimeout     suspend timeout in seconds to wait for during server suspend phase
     */
    void stop(String containerQualifier, int suspendTimeout);

}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.api;

import org.jboss.arquillian.container.test.api.ContainerController;

/**
 * {@inheritDoc}
 * <p/>
 * This extension to the original controller provides WildFly-specific lifecycle control methods.
 *
 * @author Radoslav Husar
 * @version Jan 2015
 */
public interface WildFlyContainerController extends ContainerController {

    /**
     * Stops the given container with a timeout; corresponds to {@code :shutdown(timeout=Y)} management operation.
     * <strong>Only compatible with WildFly 9 and newer!</strong>
     *
     * <p>
     * Note that if the {@code stopTimeoutInSeconds} configuration property is set at a lower value than the timeout
     * parameter the container process may be destroyed before the controlled shutdown finishes.
     * </p>
     *
     * @param containerQualifier container qualifier
     * @param timeout            timeout in seconds to wait during suspend phase
     */
    void stop(String containerQualifier, int timeout);

}

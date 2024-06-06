/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.domain.container.controller.command;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public enum Lifecycle {
    RELOAD,
    RESTART,
    RESUME,
    START,
    STOP,
    SUSPEND
}

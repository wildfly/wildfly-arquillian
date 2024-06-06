/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.junit5;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface InContainerTestAssertion {

    @Test
    default void checkInContainer() {
        final PrivilegedAction<Object> action = () -> {
            final var caller = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                    .getCallerClass();
            // We should be a modular class loader and the name should be something line deployment.${archive}
            Assertions.assertNotNull(caller.getClassLoader());
            Assertions.assertTrue(caller.getClassLoader().toString().contains("deployment."));
            return null;
        };
        if (System.getSecurityManager() == null) {
            action.run();
        } else {
            AccessController.doPrivileged(action);
        }
    }
}

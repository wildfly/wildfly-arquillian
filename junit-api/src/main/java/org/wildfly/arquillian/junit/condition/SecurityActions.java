/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.junit.condition;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Not to be made public.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class SecurityActions {

    static String getSystemProperty(final String name) {
        if (System.getSecurityManager() == null) {
            return System.getProperty(name);
        }
        return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(name));
    }

    static String resolveJBossHome() {
        // We optionally do this privileged for in-container tests
        if (System.getSecurityManager() == null) {
            String value = System.getProperty("jboss.home");
            if (value == null) {
                value = System.getenv("JBOSS_HOME");
            }
            if (value == null) {
                value = System.getProperty("jboss.home.dir");
            }
            return value;
        }
        return AccessController.doPrivileged((PrivilegedAction<String>) () -> {
            String value = System.getProperty("jboss.home");
            if (value == null) {
                value = System.getenv("JBOSS_HOME");
            }
            if (value == null) {
                value = System.getProperty("jboss.home.dir");
            }
            return value;
        });
    }
}

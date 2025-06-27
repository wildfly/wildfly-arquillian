/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.embedded;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Security actions to access system environment information. No methods in
 * this class are to be made public under any circumstances!
 *
 * @author <a href="g.grossetien@gmail.com">Guillaume Grossetie</a>
 */
class SecurityActions {

    private SecurityActions() {
    }

    static void clearSystemProperty(final String key) {
        if (System.getSecurityManager() == null) {
            System.clearProperty(key);
        } else {
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                System.clearProperty(key);
                return null;
            });
        }
    }

    static String getSystemProperty(final String key) {
        if (System.getSecurityManager() == null) {
            return System.getProperty(key);
        } else {
            return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key));
        }
    }

    static void setSystemProperty(final String key, final String value) {
        if (System.getSecurityManager() == null) {
            System.setProperty(key, value);
        } else {
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                System.setProperty(key, value);
                return null;
            });
        }
    }
}

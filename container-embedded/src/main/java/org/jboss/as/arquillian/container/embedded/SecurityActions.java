/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.embedded;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.wildfly.security.manager.WildFlySecurityManager;

/**
 * Security actions to access system environment information. No methods in
 * this class are to be made public under any circumstances!
 *
 * @author <a href="g.grossetien@gmail.com">Guillaume Grossetie</a>
 */
class SecurityActions {

    private SecurityActions() {
    }

    static void setSystemProperty(final String key, final String value) {
        if (!WildFlySecurityManager.isChecking()) {
            System.setProperty(key, value);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {

                @Override
                public Void run() {
                    System.setProperty(key, value);
                    return null;
                }
            });
        }
    }
}

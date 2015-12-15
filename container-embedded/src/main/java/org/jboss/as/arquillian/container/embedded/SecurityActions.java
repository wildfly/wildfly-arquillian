/*
 * Copyright 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.arquillian.container.embedded;

import java.security.AccessController;
import java.security.PrivilegedAction;
import org.wildfly.security.manager.WildFlySecurityManager;

/**
 * Security actions to access system environment information.  No methods in
 * this class are to be made public under any circumstances!
 *
 * @author <a href="g.grossetien@gmail.com">Guillaume Grossetie</a>
 */
class SecurityActions {

    private SecurityActions() {
    }

    static void setSystemProperty(final String key, final String value) {
        if (! WildFlySecurityManager.isChecking()) {
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

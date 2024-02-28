/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

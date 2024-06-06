/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.embedded.archive;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Test CDI Bean
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
@ApplicationScoped
public class GreetingService {

    public static final String GREETING_PREPENDED = "Hello, ";

    /**
     * Prepends the specified name with {@link GreetingService#GREETING_PREPENDED}
     *
     * @param name
     * @return
     */
    public String greet(final String name) {
        return GREETING_PREPENDED + name;
    }
}

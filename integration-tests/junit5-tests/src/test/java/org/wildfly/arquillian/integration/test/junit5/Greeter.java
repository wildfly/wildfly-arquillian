/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.junit5;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ApplicationScoped
public class Greeter {

    public String greet(String name) {
        return "Hello " + name + "!";
    }
}

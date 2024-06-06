/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.remote.archive;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * GreetingService
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
@ApplicationScoped
public class GreetingService {
    public String greet(String name) {
        return "Hello " + name + "!";
    }
}

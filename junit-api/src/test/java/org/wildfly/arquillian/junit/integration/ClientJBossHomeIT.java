/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.junit.integration;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ArquillianTest
@RunAsClient
public class ClientJBossHomeIT extends InContainerJBossHomeIT {

    @Test
    @Override
    public void assertEnvironment() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Assertions.assertNotEquals("deployment." + InContainerJBossHomeIT.class.getSimpleName() + ".war",
                classLoader.getName());
    }
}

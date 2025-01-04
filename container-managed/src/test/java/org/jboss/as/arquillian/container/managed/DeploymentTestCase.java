/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests basic deployment
 */
@ExtendWith(ArquillianExtension.class)
public class DeploymentTestCase {

    @Deployment
    public static JavaArchive create() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
                .addClass(SystemPropertyServiceActivator.class)
                .addAsServiceProvider(ServiceActivator.class, SystemPropertyServiceActivator.class);
        return archive;
    }

    @Test
    public void testSystemPropSet() throws Exception {
        Assertions.assertEquals(SystemPropertyServiceActivator.VALUE,
                System.getProperty(SystemPropertyServiceActivator.TEST_PROPERTY));
    }
}
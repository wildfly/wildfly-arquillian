/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.junit.integration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.arquillian.junit.annotations.RequiresModule;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ArquillianTest
@ApplicationScoped
public class InContainerRequireModuleIT {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(Greeter.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private Greeter greeter;

    @RequiresModule("org.jboss.as.ejb3")
    @Test
    public void expectSkipped() {
        Assertions.fail("Test should have been skipped.");
    }

    @RequiresModule(value = "jakarta.ws.rs.api", minVersion = "3.1.0")
    @Test
    public void pass() {
        Assertions.assertNotNull(greeter);
        Assertions.assertEquals("Hello!", greeter.hello());
    }

    @ApplicationScoped
    public static class Greeter {

        public String hello() {
            return "Hello!";
        }
    }
}

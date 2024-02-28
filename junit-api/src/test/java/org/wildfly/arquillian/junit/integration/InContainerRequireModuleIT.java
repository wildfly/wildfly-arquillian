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

package org.wildfly.arquillian.junit.integration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.arquillian.junit.annotations.RequiresModule;
import org.wildfly.arquillian.junit.annotations.WildFlyArquillian;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@WildFlyArquillian
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

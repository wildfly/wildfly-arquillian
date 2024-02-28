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

import jakarta.ejb.Stateless;
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
@RequiresModule("org.jboss.as.ejb3")
public class RequiresModuleIT {

    @Inject
    private SimpleEjb simpleEjb;

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(SimpleEjb.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xlm");
    }

    @Test
    public void ejbNotNull() {
        Assertions.fail("Test should have been skipped.");
    }

    @ApplicationScoped
    @Stateless
    public static class SimpleEjb {

    }
}

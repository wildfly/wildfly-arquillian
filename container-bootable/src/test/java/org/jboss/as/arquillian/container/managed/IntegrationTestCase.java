/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.as.arquillian.container.managed.archive.GreetingService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * IntegrationTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
@ExtendWith(ArquillianExtension.class)
public class IntegrationTestCase {

    @Deployment
    public static JavaArchive create() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class).addClass(GreetingService.class);
        archive.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }

    @Inject
    private GreetingService service;

    @Test
    public void shouldBeAbleToInject() throws Exception {
        Assertions.assertNotNull(service);
        Assertions.assertEquals("Hello Earthling!", service.greet("Earthling"));
    }

    @Test
    public void shouldBeAbleToFetchSystemProperties() throws Exception {
        final String prop1 = System.getProperties().getProperty("org.jboss.as.arquillian.container.managed.prop1");
        final String prop2 = System.getProperties().getProperty("org.jboss.as.arquillian.container.managed.prop2");
        Assertions.assertEquals("prop1", prop1);
        Assertions.assertEquals("prop2", prop2);
    }

}
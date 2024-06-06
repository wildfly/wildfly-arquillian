/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.container.managed.archive.GreetingService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * IntegrationTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
@RunWith(Arquillian.class)
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
        Assert.assertNotNull(service);
        Assert.assertEquals("Hello Earthling!", service.greet("Earthling"));
    }

    @Test
    public void shouldBeAbleToFetchSystemProperties() throws Exception {
        final String prop1 = System.getProperties().getProperty("org.jboss.as.arquillian.container.managed.prop1");
        final String prop2 = System.getProperties().getProperty("org.jboss.as.arquillian.container.managed.prop2");
        Assert.assertEquals("prop1", prop1);
        Assert.assertEquals("prop2", prop2);
    }

}
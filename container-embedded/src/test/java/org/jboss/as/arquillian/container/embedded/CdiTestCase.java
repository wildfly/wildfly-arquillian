/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.embedded;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.container.embedded.archive.GreetingService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests CDI and injection
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
@RunWith(Arquillian.class)
public class CdiTestCase {

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
        final String name = "ALR";
        Assert.assertEquals(GreetingService.GREETING_PREPENDED + name, service.greet(name));
    }
}
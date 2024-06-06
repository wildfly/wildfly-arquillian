/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.testng;

import java.net.URI;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Ensures that the basic startup/deployment etc facilities of the Arquillian container are working with TestNG w/ AS7. AS7-1303
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class BasicTestNGIntegrationTestCase extends Arquillian {

    @Deployment
    public static JavaArchive create() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        return archive;
    }

    @ArquillianResource
    private URI uri;

    @Test
    public void shouldBeAbleToInject() throws Exception {
        Assert.assertNotNull(uri);
    }
}

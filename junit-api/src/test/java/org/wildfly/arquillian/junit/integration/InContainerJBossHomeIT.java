/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.junit.integration;

import java.io.File;
import java.nio.file.Path;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.arquillian.junit.annotations.JBossHome;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ArquillianTest
public class InContainerJBossHomeIT {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, InContainerJBossHomeIT.class.getSimpleName() + ".war")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void assertEnvironment() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Assertions.assertEquals("deployment." + InContainerJBossHomeIT.class.getSimpleName() + ".war", classLoader.getName());
    }

    @Test
    public void stringSet(@JBossHome final String value) {
        Assertions.assertNotNull(value, "Expected the JBossHome to be set to a java.lang.String");
    }

    @Test
    public void pathSet(@JBossHome final Path value) {
        Assertions.assertNotNull(value, "Expected the JBossHome to be set to a java.nio.file.Path");
    }

    @Test
    public void fileSet(@JBossHome final File value) {
        Assertions.assertNotNull(value, "Expected the JBossHome to be set to a java.io.File");
    }
}

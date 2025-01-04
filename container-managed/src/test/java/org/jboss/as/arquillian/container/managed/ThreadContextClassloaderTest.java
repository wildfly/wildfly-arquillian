/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ArquillianExtension.class)
@Tag("JmxProtocol")
@Disabled("WFARQ-153 - The enableThreadContextClassLoader setting does not work with JUnit 5")
public class ThreadContextClassloaderTest {

    @Deployment
    public static JavaArchive deployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "tccl-tests");
        return archive;
    }

    @BeforeAll
    public static void beforeClass() throws Exception {
        assertNoTCCL();
    }

    @AfterAll
    public static void afterClass() throws Exception {
        assertNoTCCL();
    }

    @BeforeEach
    public void before() throws Exception {
        assertNoTCCL();
    }

    @AfterEach
    public void after() throws Exception {
        assertNoTCCL();
    }

    @Test
    public void testClassLoader() throws Exception {
        assertNoTCCL();
    }

    private static void assertNoTCCL() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Assertions.assertFalse(tccl instanceof ModuleClassLoader, "TCCL not ModuleClassLoader: " + tccl);
    }
}

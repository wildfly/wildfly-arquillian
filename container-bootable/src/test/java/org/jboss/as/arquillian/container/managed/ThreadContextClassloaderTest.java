/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class ThreadContextClassloaderTest {

    @Deployment
    public static JavaArchive deployment() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "tccl-tests");
        return archive;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        assertNoTCCL();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        assertNoTCCL();
    }

    @Before
    public void before() throws Exception {
        assertNoTCCL();
    }

    @After
    public void after() throws Exception {
        assertNoTCCL();
    }

    @Test
    public void testClassLoader() throws Exception {
        assertNoTCCL();
    }

    private static void assertNoTCCL() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Assert.assertFalse("TCCL not ModuleClassLoader: " + tccl, tccl instanceof ModuleClassLoader);
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.junit5.server.setup;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.arquillian.integration.test.junit5.GreeterServlet;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("NewClassNamingConvention")
@ExtendWith(ArquillianExtension.class)
public class InContainerAssumptionServerSetup extends AbstractAssumptionTestCase {

    @Deployment
    public static WebArchive create() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(GreeterServlet.class, ServerSetupTask.class);
    }

    @BeforeAll
    public static void failBeforeAll(final TestInfo testInfo) {
        Assertions.fail(() -> String.format("%s.%s should not have executed.", testInfo.getTestClass()
                .orElseThrow().getSimpleName(),
                testInfo.getTestMethod().orElseThrow().getName()));
    }

    @BeforeEach
    public void failBefore(final TestInfo testInfo) {
        Assertions.fail(() -> String.format("%s.%s should not have executed.", testInfo.getTestClass()
                .orElseThrow().getSimpleName(),
                testInfo.getTestMethod().orElseThrow().getName()));
    }

    @AfterAll
    public static void failAfterAll(final TestInfo testInfo) {
        Assertions.fail(() -> String.format("%s.%s should not have executed.", testInfo.getTestClass()
                .orElseThrow().getSimpleName(),
                testInfo.getTestMethod().orElseThrow().getName()));
    }

    @AfterEach
    public void failAfter(final TestInfo testInfo) {
        Assertions.fail(() -> String.format("%s.%s should not have executed.", testInfo.getTestClass()
                .orElseThrow().getSimpleName(),
                testInfo.getTestMethod().orElseThrow().getName()));
    }
}

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

package org.wildfly.arquillian.integration.test.junit5.server.setup;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
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
@RunAsClient
public class AssumptionServerSetup extends AbstractAssumptionTestCase {

    @Deployment
    public static WebArchive create() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(GreeterServlet.class);
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

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

package org.wildfly.arquillian.junit.requires.module;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.EventConditions;
import org.wildfly.arquillian.junit.annotations.JBossHome;

/**
 * Tests for the {@link org.wildfly.arquillian.junit.annotations.RequiresModule} annotation.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Tag("env.var")
@Tag("system.property")
public class RequiresModuleTestCase {

    @BeforeAll
    public static void setup(@JBossHome final Path jbossHome) throws Exception {
        // Create the JAR with a manifest only
        final Path jarPath = jbossHome.resolve(
                Path.of("modules", "org", "wildfly", "arquillian", "junit", "test", "resource-root", "main",
                        "test-2.0.0.Final.jar"));
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, "2.0.0.Final");
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
            // Simply flush to write the manifest
            out.flush();
        }
    }

    @Test
    public void artifactPassed() {
        final var testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(RequireArtifact.class, "passing"))
                .execute()
                .testEvents();

        testEvents.assertStatistics((stats) -> stats.succeeded(1L));
    }

    @Test
    public void artifactSkippedVersion() {
        final var testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(RequireArtifact.class, "skippedVersion"))
                .execute()
                .testEvents();

        testEvents.assertStatistics((stats) -> stats.skipped(1L));
        testEvents.assertThatEvents().haveExactly(1, EventConditions.event(
                EventConditions.skippedWithReason(
                        "Found version 1.0.0.Final and required a minimum of version 2.0.0. Disabling test.")));
    }

    @Test
    public void artifactSkippedMissingModule(@JBossHome final Path jbossHome) {
        final var testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(RequireArtifact.class, "skippedMissingModule"))
                .execute()
                .testEvents();

        testEvents.assertStatistics((stats) -> stats.skipped(1L));
        testEvents.assertThatEvents().haveExactly(1, EventConditions.event(
                EventConditions.skippedWithReason(
                        String.format(
                                "Module org.wildfly.arquillian.junit.test.artifact.invalid not found in %s. Disabling test.",
                                jbossHome.resolve("modules")))));
    }

    @Test
    public void resourceRootPassed() {
        final var testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(RequireResourceRoot.class, "passing"))
                .execute()
                .testEvents();

        testEvents.assertStatistics((stats) -> stats.succeeded(1L));
    }

    @Test
    public void resourceRootPassedVersion() {
        final var testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(RequireResourceRoot.class, "passingVersion"))
                .execute()
                .testEvents();

        testEvents.assertStatistics((stats) -> stats.succeeded(1L));
    }

    @Test
    public void resourceRootSkippedVersion() {
        final var testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(RequireResourceRoot.class, "skippedVersion"))
                .execute()
                .testEvents();

        testEvents.assertStatistics((stats) -> stats.skipped(1L));
        testEvents.assertThatEvents().haveExactly(1, EventConditions.event(
                EventConditions.skippedWithReason(
                        "Found version 2.0.0.Final and required a minimum of version 2.0.1. Disabling test.")));
    }

    @Test
    public void resourceRootSkippedMissingModule(@JBossHome final Path jbossHome) {
        final var testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(RequireResourceRoot.class, "skippedMissingModule"))
                .execute()
                .testEvents();

        testEvents.assertStatistics((stats) -> stats.skipped(1L));
        testEvents.assertThatEvents().haveExactly(1, EventConditions.event(
                EventConditions.skippedWithReason(
                        String.format(
                                "Module org.wildfly.arquillian.junit.test.resource-root.invalid not found in %s. Disabling test.",
                                jbossHome.resolve("modules")))));
    }
}

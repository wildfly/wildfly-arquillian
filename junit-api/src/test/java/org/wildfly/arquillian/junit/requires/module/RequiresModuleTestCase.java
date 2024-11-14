/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.junit.requires.module;

import java.io.IOException;
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
        createJar("resource-root", jbossHome, "2.0.0.Final");
        createJar("snapshot", jbossHome, "1.0.0.Beta2-SNAPSHOT");
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

    @Test
    public void snapshotPassedVersion() {
        final var testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(RequireSnapshot.class, "passingVersion"))
                .execute()
                .testEvents();

        testEvents.assertStatistics((stats) -> stats.succeeded(1L));
    }

    @Test
    public void snapshotSkippedVersion() {
        final var testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(RequireSnapshot.class, "skippedVersion"))
                .execute()
                .testEvents();

        testEvents.assertStatistics((stats) -> stats.skipped(1L));
        testEvents.assertThatEvents().haveExactly(1, EventConditions.event(
                EventConditions.skippedWithReason(
                        "Found version 1.0.0.Beta2-SNAPSHOT and required a minimum of version 1.0.0.Beta3. Disabling test.")));
    }

    @Test
    public void client() {
        final var testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(RequireArtifact.class, "client"))
                .execute()
                .testEvents();

        testEvents.assertStatistics((stats) -> stats.succeeded(1L));
    }

    @Test
    public void clientApi() {
        final var testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(RequireArtifact.class, "clientApi"))
                .execute()
                .testEvents();

        testEvents.assertStatistics((stats) -> stats.succeeded(1L));
    }

    @Test
    public void clientApiTest() {
        final var testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(RequireArtifact.class, "clientApiTest"))
                .execute()
                .testEvents();

        testEvents.assertStatistics((stats) -> stats.succeeded(1L));
    }

    @Test
    public void clientSpi() {
        final var testEvents = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(RequireArtifact.class, "clientSpi"))
                .execute()
                .testEvents();

        testEvents.assertStatistics((stats) -> stats.skipped(1L));
        testEvents.assertThatEvents().haveExactly(1, EventConditions.event(
                EventConditions.skippedWithReason(
                        "Found version 1.0.0.Beta1 and required a minimum of version 1.0.0.Final. Disabling test.")));
    }

    private static void createJar(final String moduleName, final Path jbossHome, final String version) throws IOException {
        // Create the JAR with a manifest only
        final Path jarPath = jbossHome.resolve(
                Path.of("modules", "org", "wildfly", "arquillian", "junit", "test", moduleName, "main",
                        String.format("test-%s.jar", version)));
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.IMPLEMENTATION_VERSION, version);
        try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jarPath), manifest)) {
            // Simply flush to write the manifest
            out.flush();
        }
    }
}

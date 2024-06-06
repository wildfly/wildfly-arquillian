/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.tools.modules;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
// Used to run a single method last to ensure the modules directory is empty
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ModuleBuilderTest {

    @BeforeAll
    public static void createModuleDir() throws IOException {
        ModuleEnvironment.createBaseModuleDir();
    }

    @Test
    public void simpleModule(final TestInfo testInfo) throws Exception {
        final ModuleDescription moduleDescription = ModuleBuilder.of(createModuleName(testInfo))
                .addManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .build();
        Assertions.assertNotNull(moduleDescription);
        final var jarFile = moduleDescription.modulePath().resolve("test-module.jar");
        Assertions.assertTrue(Files.exists(jarFile), () -> String.format("Module %s does not exist", moduleDescription.name()));
        // Open the JAR and check for the expected files
        try (FileSystem fs = jarFs(jarFile)) {
            final var beansXml = fs.getPath("/META-INF/beans.xml");
            Assertions.assertNotNull(beansXml, "Could not find META-INF/beans.xml");
            Assertions.assertTrue(Files.exists(beansXml), "Could not find META-INF/beans.xml");
        }
        assertDeleted(moduleDescription);
    }

    @Test
    public void resourcePathModule(final TestInfo testInfo) throws Exception {
        final ModuleDescription moduleDescription = ModuleBuilder.of(createModuleName(testInfo))
                .addManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addResourcePath(".")
                .addDependencies("org.jboss.as.server")
                .build();
        Assertions.assertNotNull(moduleDescription);
        final var jarFile = moduleDescription.modulePath().resolve("test-module.jar");
        Assertions.assertTrue(Files.exists(jarFile), () -> String.format("Module %s does not exist", moduleDescription.name()));
        // Open the JAR and check for the expected files
        try (FileSystem fs = jarFs(jarFile)) {
            final var beansXml = fs.getPath("/META-INF/beans.xml");
            Assertions.assertNotNull(beansXml, "Could not find META-INF/beans.xml");
            Assertions.assertTrue(Files.exists(beansXml), "Could not find META-INF/beans.xml");
        }

        // Parse the module.xml file and assert we have the expected resources and dependencies
        parseAndAssert(moduleDescription, Set.of(".", "test-module.jar"), Set.of("org.jboss.as.server"));

        assertDeleted(moduleDescription);
    }

    @Test
    public void resourceModule(final TestInfo testInfo) throws Exception {
        // Create a simple JAR as another resource dependency
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "simple.jar")
                .addManifest();
        final ModuleDescription moduleDescription = ModuleBuilder.of(createModuleName(testInfo))
                .addManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addResourcePath(".")
                .addResource(jar)
                .addDependencies("org.jboss.as.server", "org.jboss.as.logging")
                .build();
        Assertions.assertNotNull(moduleDescription);
        final var jarFile = moduleDescription.modulePath().resolve("test-module.jar");
        Assertions.assertTrue(Files.exists(jarFile), () -> String.format("Module %s does not exist", moduleDescription.name()));
        // Open the JAR and check for the expected files
        try (FileSystem fs = jarFs(jarFile)) {
            final var beansXml = fs.getPath("/META-INF/beans.xml");
            Assertions.assertNotNull(beansXml, "Could not find META-INF/beans.xml");
            Assertions.assertTrue(Files.exists(beansXml), "Could not find META-INF/beans.xml");
        }

        // Parse the module.xml file and assert we have the expected resources and dependencies
        parseAndAssert(moduleDescription, Set.of(".", "test-module.jar", "simple.jar"),
                Set.of("org.jboss.as.server", "org.jboss.as.logging"));

        assertDeleted(moduleDescription);
    }

    @Test
    public void moduleAlias(final TestInfo testInfo) throws Exception {
        final ModuleDescription target = ModuleBuilder.of(createModuleName(testInfo))
                .addManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .build();
        final ModuleDescription alias = ModuleDescription.createAlias("org.wildfly.test.alias", target.name());
        final Document document = parseAndAssert(alias, Set.of(), Set.of());

        final Elements moduleAliasElement = document.select("module-alias");
        Assertions.assertEquals("org.wildfly.test.alias", moduleAliasElement.attr("name"),
                () -> String.format("module-alias with name org.wildfly.test.alias does not exist in: %s", moduleAliasElement));
        Assertions.assertEquals(target.name(), moduleAliasElement.attr("target-name"),
                () -> String.format("module-alias with target-name %s does not exist in: %s", target.name(),
                        moduleAliasElement));

        assertDeleted(alias, false);
        assertDeleted(target);
    }

    @Test
    public void complexModule(final TestInfo testInfo) throws Exception {
        final Set<ModuleDependency> dependencies = Set.of(
                ModuleDependency.builder("org.wildfly.testing.tools.modules.dep1")
                        .export(true)
                        .services(ModuleDependency.Services.IMPORT)
                        .optional(true)
                        .addImportFilter("META-INF/services", true)
                        .addImportFilters(ModuleDependency.Filter.of("META-INF/config", true))
                        .addImportFilter("META-INF/maven", false)
                        .addExportFilters(ModuleDependency.Filter.of("META-INF/maven", false))
                        .addExportFilter("META-INF/config", true)
                        .addExportFilter("META-INF/internal", false)
                        .build(),
                ModuleDependency.builder("org.wildfly.testing.tools.modules.dep2")
                        .optional(true)
                        .build());
        final ModuleDescription moduleDescription = ModuleBuilder.of(createModuleName(testInfo))
                .addManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addDependencies(dependencies)
                .build();

        final Document document = parseAndAssert(moduleDescription, Set.of("test-module.jar"),
                Set.of("org.wildfly.testing.tools.modules.dep1", "org.wildfly.testing.tools.modules.dep2"));
        // The org.wildfly.testing.tools.modules.dep1 module should be a complex dependency
        final Elements moduleElement = document
                .select("dependencies > module[name=\"org.wildfly.testing.tools.modules.dep1\"]");
        Assertions.assertFalse(moduleElement.isEmpty(), "Module dependencies should not be empty");
        // This should be an exported dependency
        Assertions.assertEquals("true", moduleElement.attr("export"),
                () -> String.format("Expected dependency to be exported: %s", moduleElement));
        // The services should be imported
        Assertions.assertEquals("import", moduleElement.attr("services"),
                () -> String.format("Expected the dependencies services to be imported: %s", moduleElement));
        // This should be an optional dependency
        Assertions.assertEquals("true", moduleElement.attr("optional"),
                () -> String.format("Expected dependency to be optional: %s", moduleElement));

        // We should have two import include filters and one import exclude
        final Elements importIncludes = moduleElement.select("imports > include");
        Assertions.assertEquals(2, importIncludes.size(),
                () -> String.format("Expected 2 import include filters on %s", moduleElement));
        // Check the paths for the includes
        assertSelector(importIncludes, "[path=\"META-INF/services\"]");
        assertSelector(importIncludes, "[path=\"META-INF/config\"]");
        final Elements importExcludes = moduleElement.select("imports > exclude");
        Assertions.assertEquals(1, importExcludes.size(),
                () -> String.format("Expected 1 import exclude filter on %s", moduleElement));
        // Check the paths for the excludes
        assertSelector(importExcludes, "[path=\"META-INF/maven\"]");

        // We should have 1 export include filter and two export excludes
        final Elements exportIncludes = moduleElement.select("exports > include");
        Assertions.assertEquals(1, exportIncludes.size(),
                () -> String.format("Expected 1 export include filter on %s", moduleElement));
        // Check the paths for the includes
        assertSelector(exportIncludes, "[path=\"META-INF/config\"]");
        final Elements exportExcludes = moduleElement.select("exports > exclude");
        Assertions.assertEquals(2, exportExcludes.size(),
                () -> String.format("Expected 2 export exclude filters on %s", moduleElement));
        // Check the paths for the excludes
        assertSelector(exportExcludes, "[path=\"META-INF/maven\"]");
        assertSelector(exportExcludes, "[path=\"META-INF/internal\"]");

        assertDeleted(moduleDescription);
    }

    @Test
    // Always run this last
    @Order(Integer.MAX_VALUE)
    public void assertModulesDirectoryEmpty() throws Exception {
        try (Stream<Path> pathStream = Files.walk(ModuleEnvironment.BASE_MODULE_DIR)) {
            final var paths = pathStream.filter(p -> !p.equals(ModuleEnvironment.BASE_MODULE_DIR)).collect(Collectors.toList());
            Assertions.assertTrue(paths.isEmpty(), () -> "Modules directory should be empty " + paths);
        }
    }

    private static Document parseAndAssert(final ModuleDescription moduleDescription, final Set<String> expectedResourceRoots,
            final Set<String> expectedDependencies)
            throws IOException {
        final Path moduleXml = moduleDescription.modulePath().resolve("module.xml");
        Assertions.assertTrue(Files.exists(moduleXml),
                () -> String.format("Module %s does not exist", moduleDescription.name()));
        final Document document = Jsoup.parse(Files.readString(moduleXml), Parser.xmlParser());

        // Check that we have the expected resource roots
        if (!expectedResourceRoots.isEmpty()) {
            final Elements resourceRoots = document.select("resource-root");
            Assertions.assertEquals(expectedResourceRoots.size(), resourceRoots.size(),
                    () -> String.format("Expected %d resource roots, found %s", expectedResourceRoots.size(), resourceRoots));
            for (String expectedResourceRoot : expectedResourceRoots) {
                Assertions.assertEquals(1, resourceRoots.select("[path=\"" + expectedResourceRoot + "\"]").size(),
                        String.format("Failed to find <resource-root path=\"%s\"/> in %s", expectedResourceRoot,
                                resourceRoots));
            }
        }

        // Check that we have the expected dependencies
        for (String expectedDependency : expectedDependencies) {
            final Elements dependencies = document.select("dependencies > module[name=\"" + expectedDependency + "\"]");
            Assertions.assertEquals(1, dependencies.size(),
                    () -> String.format("Expected 1 dependency, found %s", dependencies));
        }
        return document;
    }

    private static void assertSelector(final Elements element, final String selector) {
        assertSelector(element, selector, element);
    }

    private static void assertSelector(final Elements element, final String selector, final Object context) {
        Assertions.assertFalse(element.select(selector)
                .isEmpty(), () -> String.format("Expected %s in %s", selector, context));
    }

    private static void assertDeleted(final ModuleDescription moduleDescription) {
        assertDeleted(moduleDescription, true);
    }

    private static void assertDeleted(final ModuleDescription moduleDescription, final boolean recursive) {
        moduleDescription.close();
        if (recursive) {
            assertRecursiveDelete(moduleDescription.modulePath());
        } else {
            Assertions.assertTrue(Files.notExists(moduleDescription.modulePath()),
                    () -> String.format("Module %s should have been deleted.", moduleDescription.name()));
        }
    }

    private static void assertRecursiveDelete(final Path path) {
        Assertions.assertNotNull(path);
        if (path.equals(ModuleEnvironment.BASE_MODULE_DIR)) {
            return;
        }
        Assertions.assertTrue(Files.notExists(path),
                () -> String.format("Path %s should have been removed, but still exists.", path));
        assertRecursiveDelete(path.getParent());
    }

    private static String createModuleName(final TestInfo testInfo) {
        return ModuleBuilderTest.class.getPackageName() + ".module-builder-test." +
                testInfo.getTestMethod()
                        .orElseThrow(() -> new RuntimeException("Failed to get test method for " + testInfo))
                        .getName();
    }

    public static FileSystem jarFs(final Path path) throws IOException {
        // locate file system by using the syntax
        // defined in java.net.JarURLConnection
        URI uri = URI.create("jar:" + path.toUri());
        try {
            return FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException ignore) {
        }
        return FileSystems.newFileSystem(uri, Map.of());
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.junit.condition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.wildfly.arquillian.junit.annotations.RequiresModule;
import org.wildfly.plugin.tools.VersionComparator;
import org.xml.sax.SAXException;

/**
 * Evaluates conditions that a module exists with the minimum version, if defined.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class RequiresModuleExecutionCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(final ExtensionContext context) {
        return AnnotationSupport.findAnnotation(context.getElement(), RequiresModule.class)
                .map((this::checkModule))
                .orElse(ConditionEvaluationResult
                        .enabled("Could not determine the @RequiresModule was found, enabling by default"));
    }

    private ConditionEvaluationResult checkModule(final RequiresModule requiresModule) {
        // First check for the module.path, if not set use the JBoss Home resolution
        final Path moduleDir = resolveModulesDir();
        // Not set, do not disable the test
        if (moduleDir == null) {
            return ConditionEvaluationResult.enabled("The module directory could not be resolved.");
        }

        try {
            // Get the module XML file.
            final Optional<ModuleDefinition> moduleDefinition = findModuleXml(moduleDir, moduleToPath(requiresModule.value()));
            if (moduleDefinition.isPresent()) {
                if (requiresModule.minVersion().isBlank()) {
                    final var def = moduleDefinition.get();
                    if (requiresModule.value().equals(def.name)) {
                        return ConditionEvaluationResult
                                .enabled(formatReason(requiresModule, "Module %s found in %s. Enabling test.",
                                        requiresModule.value(), def.path));
                    } else {
                        return ConditionEvaluationResult
                                .disabled(
                                        formatReason(requiresModule, "Module %s not found in %s. Disabling test.",
                                                requiresModule.value(),
                                                moduleDir));
                    }
                }
                return checkVersion(requiresModule, moduleDefinition.get());
            }
        } catch (IOException e) {
            return ConditionEvaluationResult
                    .enabled("Could not find module " + requiresModule.value() + ". Enabling by default. Reason: "
                            + e.getMessage());
        }
        return ConditionEvaluationResult
                .disabled(
                        formatReason(requiresModule, "Module %s not found in %s. Disabling test.", requiresModule.value(),
                                moduleDir));
    }

    private ConditionEvaluationResult checkVersion(final RequiresModule requiresModule,
            final ModuleDefinition moduleDefinition) {
        // Resolve the version from the module.xml file
        final String version = moduleDefinition.version;
        // Likely indicates the version could not be resolved.
        if (version.isBlank()) {
            return ConditionEvaluationResult
                    .enabled(String.format("Could not determine version of module %s", moduleDefinition.path));
        }
        if (isAtLeastVersion(requiresModule.minVersion(), version)) {
            return ConditionEvaluationResult
                    .enabled(String.format("Found version %s and required a minimum of version %s. Enabling tests.",
                            version, requiresModule.minVersion()));
        }
        return ConditionEvaluationResult
                .disabled(formatReason(requiresModule,
                        "Found version %s and required a minimum of version %s. Disabling test.", version,
                        requiresModule.minVersion()));
    }

    private static String moduleToPath(final String moduleName) {
        return String.join(File.separator, moduleName.split("\\."));
    }

    private static ModuleDefinition parse(final Path moduleXmlFile) throws IOException {
        String name = "";
        String version = "";
        try (InputStream in = Files.newInputStream(moduleXmlFile)) {

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            final DocumentBuilder builder = factory.newDocumentBuilder();
            final org.w3c.dom.Document document = builder.parse(in);
            final var moduleNode = document.getDocumentElement();
            name = moduleNode.getAttributes().getNamedItem("name").getTextContent();
            final var resources = document.getElementsByTagName("resources");
            if (resources.getLength() > 0) {
                // Use only the first resources, which there should only be one of
                final var resource = resources.item(0);
                final var nodes = resource.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    final var node = nodes.item(i);
                    if (node.getNodeName().equals("artifact")) {
                        // Use the Maven GAV where the third entry should be the version
                        final var artifactName = node.getAttributes().getNamedItem("name").getTextContent();
                        final var gav = artifactName.split(":");
                        if (gav.length > 2) {
                            version = sanitizeVersion(gav[2]);
                        }
                        break;
                    } else if (node.getNodeName().equals("resource-root")) {
                        final String path = node.getAttributes().getNamedItem("path").getTextContent();
                        final Path parent = moduleXmlFile.getParent();
                        final Path jar = parent == null ? Path.of(path) : parent.resolve(path);
                        try (JarFile jarFile = new JarFile(jar.toFile())) {
                            version = extractVersionFromManifest(jarFile);
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse module XML file " + moduleXmlFile, e);
        }
        return new ModuleDefinition(moduleXmlFile, name, version);
    }

    private static String extractVersionFromManifest(final JarFile jarFile) throws IOException {
        final Manifest manifest = jarFile.getManifest();
        final var version = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        return sanitizeVersion(version);
    }

    private static String sanitizeVersion(final String version) {
        if (version == null) {
            return "";
        }
        // Skip the "-redhat" for our purposes
        final int end = version.indexOf("-redhat");
        if (end > 0) {
            return version.substring(0, end);
        }
        return version;
    }

    private static Optional<ModuleDefinition> findModuleXml(final Path dir, final String pathName) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            final Optional<Path> moduleXml = files.filter((f) -> f.toString().contains(pathName)
                    && f.getFileName().toString().equals("module.xml")).findFirst();
            if (moduleXml.isPresent()) {
                return Optional.of(parse(moduleXml.get()));
            }
        }
        return Optional.empty();
    }

    private static boolean isAtLeastVersion(final String minVersion, final String foundVersion) {
        if (foundVersion == null) {
            return false;
        }
        return foundVersion.equals(minVersion) || VersionComparator.compareVersion(true, foundVersion, minVersion) >= 0;
    }

    private static String formatReason(final RequiresModule requiresModule, final String fmt, final Object... args) {
        String msg = String.format(fmt, args);
        if (!requiresModule.issueRef().isBlank()) {
            msg = requiresModule.issueRef() + ": " + msg;
        }
        if (!requiresModule.reason().isBlank()) {
            msg = msg + " Reason: " + requiresModule.reason();
        }
        return msg;
    }

    private static Path resolveModulesDir() {
        final String moduleDir = SecurityActions.getSystemProperty("module.path");
        if (moduleDir != null) {
            return Path.of(moduleDir);
        }
        final String jbossHome = SecurityActions.resolveJBossHome();
        if (jbossHome == null) {
            return null;
        }
        return Path.of(jbossHome, "modules");
    }

    private static class ModuleDefinition {
        final Path path;
        final String name;
        final String version;

        private ModuleDefinition(final Path path, final String name, final String version) {
            this.path = path;
            this.name = name;
            this.version = version;
        }
    }
}

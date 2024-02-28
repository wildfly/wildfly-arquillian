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
            final Optional<Path> moduleXmlFile = findModuleXml(moduleDir, moduleToPath(requiresModule.value()));
            if (moduleXmlFile.isPresent()) {
                if (requiresModule.minVersion().isBlank()) {
                    return ConditionEvaluationResult
                            .enabled(formatReason(requiresModule, "Module %s found in %s. Enabling test.",
                                    requiresModule.value(), moduleXmlFile.get()));
                }
                return checkVersion(requiresModule, moduleXmlFile.get());
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

    private ConditionEvaluationResult checkVersion(final RequiresModule requiresModule, final Path moduleXmlFile) {
        try {
            // Resolve the version from the module.xml file
            final String version = version(moduleXmlFile);
            // Likely indicates the version could not be resolved.
            if (version.isBlank()) {
                return ConditionEvaluationResult
                        .enabled(String.format("Could not determine version of module %s", moduleXmlFile));
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
        } catch (IOException e) {
            return ConditionEvaluationResult
                    .enabled(String.format("Could not determine the version for module %s. Enabling by default. Reason: %s",
                            requiresModule.value(), e.getMessage()));
        }
    }

    private static String moduleToPath(final String moduleName) {
        return String.join(File.separator, moduleName.split("\\."));
    }

    private static String version(final Path moduleXmlFile) throws IOException {
        try (InputStream in = Files.newInputStream(moduleXmlFile)) {

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            final DocumentBuilder builder = factory.newDocumentBuilder();
            final org.w3c.dom.Document document = builder.parse(in);
            final var resources = document.getElementsByTagName("resources");
            if (resources.getLength() > 0) {
                // Use only the first resources, which there should only be one of
                final var resource = resources.item(0);
                final var nodes = resource.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    final var node = nodes.item(i);
                    if (node.getNodeName().equals("artifact")) {
                        // Use the Maven GAV where the third entry should be the version
                        final var name = node.getAttributes().getNamedItem("name").getTextContent();
                        final var gav = name.split(":");
                        if (gav.length > 2) {
                            return sanitizeVersion(gav[2]);
                        }
                        break;
                    } else if (node.getNodeName().equals("resource-root")) {
                        final String path = node.getAttributes().getNamedItem("path").getTextContent();
                        final Path parent = moduleXmlFile.getParent();
                        final Path jar = parent == null ? Path.of(path) : parent.resolve(path);
                        try (JarFile jarFile = new JarFile(jar.toFile())) {
                            return extractVersionFromManifest(jarFile);
                        }
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException e) {
            throw new IOException("Failed to parse module XML file " + moduleXmlFile, e);
        }
        return "";
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

    private static Optional<Path> findModuleXml(final Path dir, final String pathName) throws IOException {
        try (Stream<Path> files = Files.walk(dir)) {
            return files.filter((f) -> f.toString().contains(pathName)
                    && f.getFileName().toString().equals("module.xml")).findFirst();
        }
    }

    private static boolean isAtLeastVersion(final String minVersion, final String foundVersion) {
        if (foundVersion == null) {
            return false;
        }
        return VersionComparator.compareVersion(foundVersion, minVersion) >= 0;
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
}

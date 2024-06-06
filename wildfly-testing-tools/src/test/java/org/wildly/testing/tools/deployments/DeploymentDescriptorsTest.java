/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildly.testing.tools.deployments;

import java.io.InputStream;
import java.net.SocketPermission;
import java.security.Permission;
import java.util.Collection;
import java.util.Map;
import java.util.PropertyPermission;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class DeploymentDescriptorsTest {

    @Test
    public void jbossWebSecurityDomain() throws Exception {
        final String expected = generateJBossWebXml(Map.of("security-domain", "test"));
        try (InputStream in = DeploymentDescriptors.createJBossWebSecurityDomain("test").openStream()) {
            Assertions.assertEquals(expected, new String(in.readAllBytes()));
        }
    }

    @Test
    public void jbossWebContextRoot() throws Exception {
        final String expected = generateJBossWebXml(Map.of("context-root", "/test"));
        try (InputStream in = DeploymentDescriptors.createJBossWebContextRoot("/test").openStream()) {
            Assertions.assertEquals(expected, new String(in.readAllBytes()));
        }
    }

    @Test
    public void jbossWebXml() throws Exception {
        final var elements = Map.of("virtual-host", "localhost", "context-root", "/", "security-domain", "ApplicationDomain");
        final String expected = generateJBossWebXml(elements);
        try (InputStream in = DeploymentDescriptors.createJBossWebXmlAsset(elements).openStream()) {
            Assertions.assertEquals(expected, new String(in.readAllBytes()));
        }
    }

    @ParameterizedTest
    @MethodSource("moduleArguments")
    public void jbossDeploymentStructure(final Set<String> addedModules, final Set<String> excludedModules) {
        final String expected = generateJBossDeploymentStructure(addedModules, excludedModules);
        Assertions.assertEquals(expected,
                new String(DeploymentDescriptors.createJBossDeploymentStructure(addedModules, excludedModules)));
    }

    @ParameterizedTest
    @MethodSource("permissions")
    public void permissionsXml(final Set<Permission> permissions) {
        final String expected = generatePermissionsXml(permissions);
        Assertions.assertEquals(expected,
                new String(DeploymentDescriptors.createPermissionsXml(permissions)));
    }

    static Stream<Arguments> moduleArguments() {
        return Stream.of(
                Arguments.of(Named.of("addedModules", Set.of("org.wildfly.arquillian", "org.wildfly.arquillian.test")),
                        Named.of("excludedModules", Set.of())),
                Arguments.of(Named.of("addedModules", Set.of("org.wildfly.arquillian", "org.wildfly.arquillian.test")),
                        Named.of("excludedModules", Set.of("org.jboss.as.logging"))),
                Arguments.of(Named.of("addedModules", Set.of()),
                        Named.of("excludedModules", Set.of("org.jboss.as.logging"))));
    }

    static Stream<Arguments> permissions() {
        return Stream.of(
                Arguments.of(Set.of(new RuntimePermission("test.permissions", "action1"))),
                Arguments.of(Set.of(new SocketPermission("localhost", "connect,resolve"),
                        new PropertyPermission("java.io.tmpdir", "read"),
                        new PropertyPermission("test.property", "read,write"))));
    }

    private static String generateJBossWebXml(final Map<String, String> elements) {
        final StringBuilder xml = new StringBuilder()
                .append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("<jboss-web>")
                .append(System.lineSeparator());
        for (var element : elements.entrySet()) {
            xml.append("    <")
                    .append(element.getKey())
                    .append('>')
                    .append(element.getValue())
                    .append("</")
                    .append(element.getKey())
                    .append(">")
                    .append(System.lineSeparator());
        }
        xml.append("</jboss-web>");
        return xml.toString();
    }

    private static String generateJBossDeploymentStructure(final Set<String> addedModules, final Set<String> excludedModules) {
        final StringBuilder xml = new StringBuilder()
                .append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("<jboss-deployment-structure>")
                .append(System.lineSeparator())
                .append("    <deployment>")
                .append(System.lineSeparator());
        if (!addedModules.isEmpty()) {
            xml.append("        <dependencies>")
                    .append(System.lineSeparator());
            for (String module : addedModules) {
                xml.append("            <module name=\"").append(module).append("\">")
                        .append(System.lineSeparator())
                        .append("            </module>")
                        .append(System.lineSeparator());
            }
            xml.append("        </dependencies>")
                    .append(System.lineSeparator());
        }
        if (!excludedModules.isEmpty()) {
            xml.append("        <exclusions>")
                    .append(System.lineSeparator());
            for (String module : addedModules) {
                xml.append("            <module name=\"").append(module).append("\">")
                        .append(System.lineSeparator())
                        .append("            </module>")
                        .append(System.lineSeparator());
            }
            xml.append("        </exclusions>")
                    .append(System.lineSeparator());
        }

        xml.append("    </deployment>")
                .append(System.lineSeparator())
                .append("</jboss-deployment-structure>");
        return xml.toString();
    }

    private static String generatePermissionsXml(final Collection<Permission> permissions) {
        final StringBuilder xml = new StringBuilder()
                .append("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
                .append(System.lineSeparator())
                .append(System.lineSeparator())
                .append("<permissions xmlns=\"https://jakarta.ee/xml/ns/jakartaee\" version=\"10\">")
                .append(System.lineSeparator());
        for (Permission permission : permissions) {
            xml.append("    <permission>")
                    .append(System.lineSeparator())
                    .append("        <class-name>").append(permission.getClass().getName()).append("</class-name>")
                    .append(System.lineSeparator())
                    .append("        <name>").append(permission.getName()).append("</name>")
                    .append(System.lineSeparator());
            final String actions = permission.getActions();
            if (actions != null && !actions.isEmpty()) {
                xml.append("        <actions>").append(actions).append("</actions>")
                        .append(System.lineSeparator());
            }
            xml.append("    </permission>")
                    .append(System.lineSeparator());
        }
        xml.append("</permissions>");
        return xml.toString();
    }
}

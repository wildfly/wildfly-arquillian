/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.tools.deployments;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.container.WebContainer;
import org.wildfly.testing.tools.xml.CloseableXMLStreamWriter;

/**
 * A utility to generate various deployment descriptors.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("unused")
public class DeploymentDescriptors {

    private DeploymentDescriptors() {
    }

    /**
     * Adds a {@code jboss-deployment-structure.xml} file to a deployment with optional dependency additions or
     * exclusions.
     *
     * @param archive         the archive to add the {@code jboss-deployment-structure.xml} to
     * @param addedModules    the modules to add to an archive or an empty set
     * @param excludedModules the modules to exclude from an archive or an empty set
     * @param <T>             the archive type
     *
     * @return the archive
     */
    public static <T extends WebContainer<T> & Archive<T>> T addJBossDeploymentStructure(final T archive,
            final Set<String> addedModules, final Set<String> excludedModules) {
        return archive.addAsWebInfResource(createJBossDeploymentStructureAsset(addedModules, excludedModules),
                "jboss-deployment-structure.xml");
    }

    /**
     * Creates a {@code jboss-deployment-structure.xml} file with the optional dependency additions or exclusions.
     *
     * @param addedModules    the modules to add or an empty set
     * @param excludedModules the modules to exclude or an empty set
     *
     * @return a {@code jboss-deployment-structure.xml} asset
     */
    public static Asset createJBossDeploymentStructureAsset(final Set<String> addedModules, final Set<String> excludedModules) {
        return new ByteArrayAsset(createJBossDeploymentStructure(addedModules, excludedModules));
    }

    /**
     * Creates a {@code jboss-deployment-structure.xml} file with the optional dependency additions or exclusions.
     *
     * @param addedModules    the modules to add or an empty set
     * @param excludedModules the modules to exclude or an empty set
     *
     * @return a {@code jboss-deployment-structure.xml} in a byte array
     */
    public static byte[] createJBossDeploymentStructure(final Set<String> addedModules, final Set<String> excludedModules) {
        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                CloseableXMLStreamWriter writer = CloseableXMLStreamWriter.of(out);) {
            writer.writeStartDocument("utf-8", "1.0");
            writer.writeStartElement("jboss-deployment-structure");

            writer.writeStartElement("deployment");

            if (!addedModules.isEmpty()) {
                writer.writeStartElement("dependencies");
                for (String module : addedModules) {
                    writer.writeStartElement("module");
                    writer.writeAttribute("name", module);
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
            if (!excludedModules.isEmpty()) {
                writer.writeStartElement("exclusions");
                for (String module : addedModules) {
                    writer.writeStartElement("module");
                    writer.writeAttribute("name", module);
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }

            writer.writeEndElement();

            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            return out.toByteArray();
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException("Failed to create the jboss-deployment-structure.xml file.", e);
        }
    }

    /**
     * Creates a {@code jboss-web.xml} with the context root provided.
     *
     * @param contextRoot the context root to use for the deployment
     *
     * @return a {@code jboss-web.xml}
     */
    public static Asset createJBossWebContextRoot(final String contextRoot) {
        return createJBossWebXmlAsset(Map.of("context-root", contextRoot));
    }

    /**
     * Creates a {@code jboss-web.xml} with the security domain for the deployment.
     *
     * @param securityDomain the security domain to use for the deployment
     *
     * @return a {@code jboss-web.xml}
     */
    public static Asset createJBossWebSecurityDomain(final String securityDomain) {
        return createJBossWebXmlAsset(Map.of("security-domain", securityDomain));
    }

    /**
     * Creates a {@code jboss-web.xml} with simple attributes.
     *
     * @param elements the elements to add where the key is the element name and the value is the elements value
     *
     * @return a {@code jboss-web.xml}
     */
    public static Asset createJBossWebXmlAsset(final Map<String, String> elements) {
        return new ByteArrayAsset(createJBossWebXml(elements));
    }

    /**
     * Creates a {@code jboss-web.xml} with simple attributes.
     *
     * @param elements the elements to add where the key is the element name and the value is the elements value
     *
     * @return a {@code jboss-web.xml}
     */
    public static byte[] createJBossWebXml(final Map<String, String> elements) {
        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                CloseableXMLStreamWriter writer = CloseableXMLStreamWriter.of(out);) {
            writer.writeStartDocument("utf-8", "1.0");
            writer.writeStartElement("jboss-web");

            for (var element : elements.entrySet()) {
                writer.writeStartElement(element.getKey());
                writer.writeCharacters(element.getValue());
                writer.writeEndElement();
            }

            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            return out.toByteArray();
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException("Failed to create the jboss-deployment-structure.xml file.", e);
        }
    }

    /**
     * Creates a new asset with the given contents for a {@code permissions.xml} file.
     *
     * @param permissions the permissions to add to the file
     *
     * @return an asset with the given contents for a {@code permissions.xml} file
     */
    public static Asset createPermissionsXmlAsset(Permission... permissions) {
        return new ByteArrayAsset(createPermissionsXml(permissions));
    }

    /**
     * Creates a new asset with the given contents for a {@code permissions.xml} file.
     *
     * @param permissions           the permissions to add to the file
     * @param additionalPermissions any additional permissions to add to the file
     *
     * @return an asset with the given contents for a {@code permissions.xml} file
     */
    public static Asset createPermissionsXmlAsset(final Iterable<? extends Permission> permissions,
            final Permission... additionalPermissions) {
        return new ByteArrayAsset(createPermissionsXml(permissions, additionalPermissions));
    }

    /**
     * Creates a new asset with the given contents for a {@code permissions.xml} file.
     *
     * @param permissions the permissions to add to the file
     *
     * @return an asset with the given contents for a {@code permissions.xml} file
     */
    public static Asset createPermissionsXmlAsset(final Iterable<? extends Permission> permissions) {
        return new ByteArrayAsset(createPermissionsXml(permissions));
    }

    /**
     * Creates a new asset with the given contents for a {@code permissions.xml} file.
     *
     * @param permissions the permissions to add to the file
     *
     * @return an asset with the given contents for a {@code permissions.xml} file
     */
    public static byte[] createPermissionsXml(Permission... permissions) {
        return createPermissionsXml(List.of(permissions));
    }

    /**
     * Creates a new asset with the given contents for a {@code permissions.xml} file.
     *
     * @param permissions           the permissions to add to the file
     * @param additionalPermissions any additional permissions to add to the file
     *
     * @return an asset with the given contents for a {@code permissions.xml} file
     */
    public static byte[] createPermissionsXml(final Iterable<? extends Permission> permissions,
            final Permission... additionalPermissions) {
        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                CloseableXMLStreamWriter writer = CloseableXMLStreamWriter.of(out);) {

            writer.writeStartDocument("utf-8", "1.0");
            writer.writeStartElement("permissions");
            writer.writeNamespace(null, "https://jakarta.ee/xml/ns/jakartaee");
            writer.writeAttribute("version", "10");
            addPermissionXml(writer, permissions);
            if (additionalPermissions != null && additionalPermissions.length > 0) {
                addPermissionXml(writer, List.of(additionalPermissions));
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            return out.toByteArray();
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException("Failed to create the permissions.xml file.", e);
        }
    }

    /**
     * This should only be used as a workaround for issues with API's where something like a
     * {@link java.util.ServiceLoader} needs access to an implementation.
     * <p>
     * Adds file permissions for every JAR in the modules directory. The {@code module.jar.path} system property
     * <strong>must</strong> be set.
     * </p>
     *
     * @param moduleNames the module names to add file permissions for
     *
     * @return a collection of permissions required
     */
    public static Collection<Permission> addModuleFilePermission(final String... moduleNames) {
        final String value = System.getProperty("module.jar.path");
        if (value == null || value.isBlank()) {
            return Collections.emptySet();
        }
        // Get the module path
        final Path moduleDir = Path.of(value);
        final Collection<Permission> result = new ArrayList<>();
        for (String moduleName : moduleNames) {
            final Path definedModuleDir = moduleDir.resolve(moduleName.replace('.', File.separatorChar))
                    .resolve("main");
            // Find all the JAR's
            try (Stream<Path> stream = Files.walk(definedModuleDir)) {
                stream
                        .filter((path) -> path.getFileName().toString().endsWith(".jar"))
                        .map((path) -> new FilePermission(path.toAbsolutePath().toString(), "read"))
                        .forEach(result::add);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return result;
    }

    /**
     * Creates the permissions required for the {@code java.io.tmpdir}. This adds permissions to read the directory, then
     * adds permissions for all files and subdirectories of the temporary directory. The actions are used for the latter
     * permission.
     *
     * @param actions the actions required for the temporary directory
     *
     * @return the permissions required
     */
    public static Collection<FilePermission> createTempDirPermission(final String actions) {
        String tempDir = System.getProperty("java.io.tmpdir");
        // This should never happen, but it's a better error message than an NPE
        if (tempDir.charAt(tempDir.length() - 1) != File.separatorChar) {
            tempDir += File.separatorChar;
        }
        return List.of(new FilePermission(tempDir, "read"), new FilePermission(tempDir + "-", actions));
    }

    private static void addPermissionXml(final XMLStreamWriter writer, final Iterable<? extends Permission> permissions)
            throws XMLStreamException {
        for (Permission permission : permissions) {
            writer.writeStartElement("permission");

            writer.writeStartElement("class-name");
            writer.writeCharacters(permission.getClass().getName());
            writer.writeEndElement();

            writer.writeStartElement("name");
            writer.writeCharacters(permission.getName());
            writer.writeEndElement();

            final String actions = permission.getActions();
            if (actions != null && !actions.isEmpty()) {
                writer.writeStartElement("actions");
                writer.writeCharacters(actions);
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.tools.deployments;

import java.io.FilePermission;
import java.security.Permission;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.container.WebContainer;

/**
 * A utility to generate various deployment descriptors.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @deprecated use the new WildFly Testing Tools project
 */
@Deprecated(forRemoval = true, since = "6.0")
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
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.addJBossDeploymentStructure(archive, addedModules,
                excludedModules);
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
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.createJBossDeploymentStructureAsset(addedModules,
                excludedModules);
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
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.createJBossDeploymentStructure(addedModules,
                excludedModules);
    }

    /**
     * Creates a {@code jboss-web.xml} with the context root provided.
     *
     * @param contextRoot the context root to use for the deployment
     *
     * @return a {@code jboss-web.xml}
     */
    public static Asset createJBossWebContextRoot(final String contextRoot) {
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.createJBossWebContextRoot(contextRoot);
    }

    /**
     * Creates a {@code jboss-web.xml} with the security domain for the deployment.
     *
     * @param securityDomain the security domain to use for the deployment
     *
     * @return a {@code jboss-web.xml}
     */
    public static Asset createJBossWebSecurityDomain(final String securityDomain) {
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.createJBossWebSecurityDomain(securityDomain);
    }

    /**
     * Creates a {@code jboss-web.xml} with simple attributes.
     *
     * @param elements the elements to add where the key is the element name and the value is the elements value
     *
     * @return a {@code jboss-web.xml}
     */
    public static Asset createJBossWebXmlAsset(final Map<String, String> elements) {
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.createJBossWebXmlAsset(elements);
    }

    /**
     * Creates a {@code jboss-web.xml} with simple attributes.
     *
     * @param elements the elements to add where the key is the element name and the value is the elements value
     *
     * @return a {@code jboss-web.xml}
     */
    public static byte[] createJBossWebXml(final Map<String, String> elements) {
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.createJBossWebXml(elements);
    }

    /**
     * Creates a new asset with the given contents for a {@code permissions.xml} file.
     *
     * @param permissions the permissions to add to the file
     *
     * @return an asset with the given contents for a {@code permissions.xml} file
     */
    public static Asset createPermissionsXmlAsset(Permission... permissions) {
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.createPermissionsXmlAsset(permissions);
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
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.createPermissionsXmlAsset(permissions,
                additionalPermissions);
    }

    /**
     * Creates a new asset with the given contents for a {@code permissions.xml} file.
     *
     * @param permissions the permissions to add to the file
     *
     * @return an asset with the given contents for a {@code permissions.xml} file
     */
    public static Asset createPermissionsXmlAsset(final Iterable<? extends Permission> permissions) {
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.createPermissionsXmlAsset(permissions);
    }

    /**
     * Creates a new asset with the given contents for a {@code permissions.xml} file.
     *
     * @param permissions the permissions to add to the file
     *
     * @return an asset with the given contents for a {@code permissions.xml} file
     */
    public static byte[] createPermissionsXml(Permission... permissions) {
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.createPermissionsXml(permissions);
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
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.createPermissionsXml(permissions,
                additionalPermissions);
    }

    /**
     * Creates a new asset with the new permissions appended to the current permissions. Note that duplicates will not
     * be added. A duplicates is considered a {@link Permission} with the same {@linkplain Class#getName() class name},
     * same {@linkplain Permission#getName() name} and same {@linkplain Permission#getActions() actions}.
     *
     * @param currentPermissions the current permissions, must be valid XML content
     * @param permissions        the permissions to add
     *
     * @return a new asset to replace the current {@code permissions.xml} file
     */
    public static Asset appendPermissions(final Asset currentPermissions, final Permission... permissions) {
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.appendPermissions(currentPermissions, permissions);
    }

    /**
     * Creates a new asset with the new permissions appended to the current permissions. Note that duplicates will not
     * be added. A duplicates is considered a {@link Permission} with the same {@linkplain Class#getName() class name},
     * same {@linkplain Permission#getName() name} and same {@linkplain Permission#getActions() actions}.
     *
     * @param currentPermissions the current permissions, must be valid XML content
     * @param permissions        the permissions to add
     *
     * @return a new asset to replace the current {@code permissions.xml} file
     */
    public static Asset appendPermissions(final Asset currentPermissions, final Iterable<? extends Permission> permissions) {
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.appendPermissions(currentPermissions, permissions);
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
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.addModuleFilePermission(moduleNames);
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
        return org.wildfly.testing.tools.deployment.DeploymentDescriptors.createTempDirPermission(actions);
    }
}

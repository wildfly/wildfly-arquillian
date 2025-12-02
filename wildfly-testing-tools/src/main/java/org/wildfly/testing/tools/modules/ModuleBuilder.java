/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.tools.modules;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * A simple utility to create a module.
 * <p>
 * This will create a JAR based on the classes and generate a module.xml file.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @deprecated use the new WildFly Testing Tools project
 */
@Deprecated(forRemoval = true, since = "6.0")
@SuppressWarnings({ "unused", "UnusedReturnValue", "removal" })
public class ModuleBuilder {
    private final org.wildfly.testing.tools.module.ModuleBuilder delegate;

    private ModuleBuilder(final org.wildfly.testing.tools.module.ModuleBuilder delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a new module builder with an archive name of test-module.jar.
     *
     * @param moduleName the name for the module
     *
     * @return a new module builder
     */
    public static ModuleBuilder of(final String moduleName) {
        return of(moduleName, (String) null);
    }

    /**
     * Creates a new module builder with an archive name of test-module.jar.
     *
     * @param moduleName the name for the module
     * @param jar        the JAR to use for the module
     *
     * @return a new module builder
     */
    public static ModuleBuilder of(final String moduleName, final JavaArchive jar) {
        return of(moduleName, jar, null);
    }

    /**
     * Creates a new module builder.
     *
     * @param moduleName  the name for the module
     * @param archiveName the name for the archive
     *
     * @return a new module builder
     */
    public static ModuleBuilder of(final String moduleName, final String archiveName) {
        return new ModuleBuilder(org.wildfly.testing.tools.module.ModuleBuilder.of(moduleName, archiveName, null));
    }

    /**
     * Creates a new module builder.
     *
     * @param moduleName  the name for the module
     * @param archiveName the name for the archive
     * @param modulePath  the JBoss Modules path where the module should be created, if {@code null} there will be an attempt to
     *                        discover the path
     *
     * @return a new module builder
     */
    public static ModuleBuilder of(final String moduleName, final String archiveName, final Path modulePath) {
        return new ModuleBuilder(org.wildfly.testing.tools.module.ModuleBuilder.of(moduleName, archiveName, modulePath));
    }

    /**
     * Creates a new module builder.
     *
     * @param moduleName the name for the module
     * @param jar        the JAR to use for the module
     * @param modulePath the JBoss Modules path where the module should be created, if {@code null} there will be an attempt to
     *                       discover the path
     *
     * @return a new module builder
     */
    public static ModuleBuilder of(final String moduleName, final JavaArchive jar, final Path modulePath) {
        return new ModuleBuilder(org.wildfly.testing.tools.module.ModuleBuilder.of(moduleName, jar, modulePath));
    }

    /**
     * Returns the module name.
     *
     * @return the module name
     */
    public String name() {
        return delegate.name();
    }

    /**
     * Adds an asset to the module library.
     *
     * @param asset  the asset to add
     * @param target the target path for the asset
     *
     * @return this builder
     */
    public ModuleBuilder addAsset(final Asset asset, final String target) {
        delegate.addAsset(asset, target);
        return this;
    }

    /**
     * Adds a class to the module to be generated.
     *
     * @param c the class to add
     *
     * @return this builder
     */
    public ModuleBuilder addClass(final Class<?> c) {
        delegate.addClass(c);
        return this;
    }

    /**
     * Adds the classes to the module to be generated.
     *
     * @param classes the classes to add
     *
     * @return this builder
     */
    public ModuleBuilder addClasses(final Class<?>... classes) {
        delegate.addClasses(classes);
        return this;
    }

    /**
     * Adds a dependency for the module.xml file.
     *
     * @param dependency the dependency to add
     *
     * @return this builder
     */
    public ModuleBuilder addDependency(final String dependency) {
        delegate.addDependency(dependency);
        return this;
    }

    /**
     * Adds the dependencies for the module.xml file.
     *
     * @param dependencies the dependencies to add
     *
     * @return this builder
     */
    public ModuleBuilder addDependencies(final String... dependencies) {
        delegate.addDependencies(dependencies);
        return this;
    }

    /**
     * Adds a dependency for the module.xml file.
     *
     * @param dependency the dependency to add
     *
     * @return this builder
     */
    public ModuleBuilder addDependency(final ModuleDependency dependency) {
        delegate.addDependency(dependency.delegate());
        return this;
    }

    /**
     * Adds the dependencies for the module.xml file.
     *
     * @param dependencies the dependencies to add
     *
     * @return this builder
     */
    public ModuleBuilder addDependencies(final ModuleDependency... dependencies) {
        delegate.addDependencies(ModuleDependency.map(List.of(dependencies)));
        return this;
    }

    /**
     * Adds the dependencies for the module.xml file.
     *
     * @param dependencies the dependencies to add
     *
     * @return this builder
     */
    public ModuleBuilder addDependencies(final Collection<ModuleDependency> dependencies) {
        delegate.addDependencies(ModuleDependency.map(dependencies));
        return this;
    }

    /**
     * Creates a {@code META-INF/services} file for the interface with the implementations provied.
     *
     * @param intf            the interface to crate the services file for
     * @param implementations the implemenations
     *
     * @return this builder
     */
    public ModuleBuilder addServiceProvider(final Class<?> intf, final Class<?>... implementations) {
        delegate.addServiceProvider(intf, implementations);
        return this;
    }

    /**
     * Adds all the classes in the {@linkplain Package package} to the generated module.
     *
     * @param p the package to add
     *
     * @return this builder
     */
    public ModuleBuilder addPackage(final String p) {
        delegate.addPackage(p);
        return this;
    }

    /**
     * Adds all the classes in the {@linkplain Package package} to the generated module.
     *
     * @param p the package to add
     *
     * @return this builder
     */
    public ModuleBuilder addPackage(final Package p) {
        delegate.addPackage(p);
        return this;
    }

    /**
     * Adds a manifest resource, {@code META-INF}, inside the module JAR.
     *
     * @param asset  the resource to add
     * @param target the target path within the archive in which to add the resource, relative to the {@code META-INF} path
     *
     * @return this builder
     */
    public ModuleBuilder addManifestResource(final Asset asset, final String target) {
        delegate.addManifestResource(asset, target);
        return this;
    }

    /**
     * Adds another resource root library to the module.
     *
     * @param resource the resource to add
     *
     * @return this builder
     */
    public ModuleBuilder addResource(final JavaArchive resource) {
        delegate.addResource(resource);
        return this;
    }

    /**
     * Adds more resource root libraries to the module.
     *
     * @param resources the resources to add
     *
     * @return this builder
     */
    public ModuleBuilder addResources(final JavaArchive... resources) {
        delegate.addResources(resources);
        return this;
    }

    /**
     * Adds more resource root libraries to the module.
     *
     * @param resources the resources to add
     *
     * @return this builder
     */
    public ModuleBuilder addResources(final Collection<JavaArchive> resources) {
        delegate.addResources(resources);
        return this;
    }

    /**
     * Adds a resource path to be added to the root resource.
     *
     * @param resourcePath the resource path to add
     *
     * @return this builder
     */
    public ModuleBuilder addResourcePath(final String resourcePath) {
        delegate.addResourcePath(resourcePath);
        return this;
    }

    /**
     * Adds the resource paths to be added to the root resource.
     *
     * @param resourcePaths the resource paths to add
     *
     * @return this builder
     */
    public ModuleBuilder addResourcePaths(final String... resourcePaths) {
        delegate.addResourcePaths(resourcePaths);
        return this;
    }

    /**
     * Adds the resource paths to be added to the root resource.
     *
     * @param resourcePaths the resource paths to add
     *
     * @return this builder
     */
    public ModuleBuilder addResourcePaths(final Set<String> resourcePaths) {
        delegate.addResourcePaths(resourcePaths);
        return this;
    }

    /**
     * Creates the module by:
     * <ul>
     * <li>Creating the module directory based on the modules name</li>
     * <li>Generating a JAR file for the resource</li>
     * <li>Generating a module.xml file</li>
     * </ul>
     * <p>
     * The returned cleanup task will attempt to delete the module directory. There are some cases where the module
     * directory may not be able to be deleted. The {@link IOException} is caught in these cases and a
     * {@linkplain Runtime#addShutdownHook(Thread) shutdown hook} is added to delete the files when the JVM exits.
     * </p>
     *
     * @return a task to clean up the module
     */
    public ModuleDescription build() {
        return new ModuleDescription(delegate.build());
    }
}

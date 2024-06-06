/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.tools.modules;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.testing.tools.xml.CloseableXMLStreamWriter;

/**
 * A simple utility to create a module.
 * <p>
 * This will create a JAR based on the classes and generate a module.xml file.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings({ "unused", "UnusedReturnValue" })
public class ModuleBuilder {

    private final String name;
    private final Path modulePath;
    private final JavaArchive jar;
    private final Set<String> resourcePaths;
    private final Set<JavaArchive> resources;
    private final Set<ModuleDependency> dependencies;

    private ModuleBuilder(final String name, final JavaArchive jar, final Path modulePath) {
        this.name = name;
        this.modulePath = modulePath == null ? Modules.discoverModulePath() : modulePath;
        this.jar = jar;
        dependencies = new LinkedHashSet<>();
        resources = new LinkedHashSet<>();
        this.resourcePaths = new LinkedHashSet<>();
    }

    private ModuleBuilder(final String name, final String archiveName, final Path modulePath) {
        this(name, ShrinkWrap.create(JavaArchive.class, archiveName == null ? "test-module.jar" : archiveName), modulePath);
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
        return new ModuleBuilder(moduleName, archiveName, null);
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
        return new ModuleBuilder(moduleName, archiveName, modulePath);
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
        return new ModuleBuilder(moduleName, jar, modulePath);
    }

    /**
     * Returns the module name.
     *
     * @return the module name
     */
    public String name() {
        return name;
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
        jar.add(asset, target);
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
        jar.addClass(c);
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
        jar.addClasses(classes);
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
        this.dependencies.add(ModuleDependency.builder(dependency).build());
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
        for (String dependency : dependencies) {
            addDependency(dependency);
        }
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
        this.dependencies.add(dependency);
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
        return addDependencies(Set.of(dependencies));
    }

    /**
     * Adds the dependencies for the module.xml file.
     *
     * @param dependencies the dependencies to add
     *
     * @return this builder
     */
    public ModuleBuilder addDependencies(final Collection<ModuleDependency> dependencies) {
        this.dependencies.addAll(dependencies);
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
        validate(intf, implementations);
        jar.addAsServiceProvider(intf, implementations);
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
        jar.addPackage(p);
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
        jar.addPackage(p);
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
        jar.addAsManifestResource(asset, target);
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
        this.resources.add(resource);
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
        this.resources.addAll(Set.of(resources));
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
        this.resources.addAll(resources);
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
        this.resourcePaths.add(resourcePath);
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
        this.resourcePaths.addAll(Set.of(resourcePaths));
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
        this.resourcePaths.addAll(resourcePaths);
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
        try {
            final Path mp = modulePath;
            final Path moduleDir = mp.resolve(name.replace('.', File.separatorChar)).resolve("main");
            if (Files.notExists(moduleDir)) {
                Files.createDirectories(moduleDir);
            }
            final Path fullPathToDelete = moduleDir.subpath(0, mp.getNameCount() + 1);
            createModule(moduleDir);
            return new ModuleDescription(name, mp, moduleDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void createModule(final Path moduleDir) throws IOException {
        Files.createDirectories(moduleDir);
        try (CloseableXMLStreamWriter writer = CloseableXMLStreamWriter
                .of(Files.newBufferedWriter(moduleDir.resolve("module.xml")))) {
            writer.writeStartDocument("utf-8", "1.0");
            writer.writeStartElement("module");
            writer.writeNamespace(null, "urn:jboss:module:1.9");
            writer.writeAttribute("name", name);

            writer.writeStartElement("resources");
            writer.writeEmptyElement("resource-root");
            writer.writeAttribute("path", jar.getName());
            for (String resource : resourcePaths) {
                writer.writeEmptyElement("resource-root");
                writer.writeAttribute("path", resource);
            }
            for (JavaArchive resource : resources) {
                writer.writeEmptyElement("resource-root");
                writer.writeAttribute("path", resource.getName());

                // Create the JAR
                try (
                        OutputStream out = Files.newOutputStream(moduleDir.resolve(resource.getName()),
                                StandardOpenOption.CREATE_NEW)) {
                    resource.as(ZipExporter.class).exportTo(out);
                }
            }
            writer.writeEndElement();

            // Write the dependencies
            if (!dependencies.isEmpty()) {
                writer.writeStartElement("dependencies");
                for (ModuleDependency dependency : dependencies) {
                    final boolean emptyElement = dependency.imports().isEmpty() && dependency.exports().isEmpty();
                    if (emptyElement) {
                        writer.writeEmptyElement("module");
                    } else {
                        writer.writeStartElement("module");
                    }
                    writer.writeAttribute("name", dependency.name());
                    if (dependency.isExport()) {
                        writer.writeAttribute("export", "true");
                    }
                    if (dependency.isOptional()) {
                        writer.writeAttribute("optional", "true");
                    }
                    if (dependency.services().isPresent()) {
                        writer.writeAttribute("services", dependency.services().get().toString());
                    }
                    if (!emptyElement) {
                        if (!dependency.imports().isEmpty()) {
                            writer.writeStartElement("imports");
                            for (ModuleDependency.Filter filter : dependency.imports()) {
                                if (filter.include()) {
                                    writer.writeEmptyElement("include");
                                    writer.writeAttribute("path", filter.path());
                                } else {
                                    writer.writeEmptyElement("exclude");
                                    writer.writeAttribute("path", filter.path());
                                }
                            }
                            writer.writeEndElement();
                        }
                        if (!dependency.exports().isEmpty()) {
                            writer.writeStartElement("exports");
                            for (ModuleDependency.Filter filter : dependency.exports()) {
                                if (filter.include()) {
                                    writer.writeEmptyElement("include");
                                    writer.writeAttribute("path", filter.path());
                                } else {
                                    writer.writeEmptyElement("exclude");
                                    writer.writeAttribute("path", filter.path());
                                }
                            }
                            writer.writeEndElement();
                        }
                        writer.writeEndElement(); // end module
                    }
                }
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }

        // Create the JAR
        try (OutputStream out = Files.newOutputStream(moduleDir.resolve(jar.getName()), StandardOpenOption.CREATE_NEW)) {
            jar.as(ZipExporter.class).exportTo(out);
        }
    }

    private static void validate(final Class<?> type, final Class<?>... subtypes) {
        final Set<Class<?>> invalidTypes = new LinkedHashSet<>();
        for (Class<?> subtype : subtypes) {
            if (!type.isAssignableFrom(subtype)) {
                invalidTypes.add(subtype);
            }
        }
        if (!invalidTypes.isEmpty()) {
            final StringBuilder msg = new StringBuilder("The following types are not subtypes of ")
                    .append(type.getCanonicalName())
                    .append(" : ");
            final Iterator<Class<?>> iter = invalidTypes.iterator();
            while (iter.hasNext()) {
                msg.append(iter.next().getCanonicalName());
                if (iter.hasNext()) {
                    msg.append(", ");
                }
            }
            throw new IllegalArgumentException(msg.toString());
        }
    }
}

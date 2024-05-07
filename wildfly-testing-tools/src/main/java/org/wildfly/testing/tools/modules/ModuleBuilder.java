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

package org.wildfly.testing.tools.modules;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
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
    private final Collection<ModuleDependency> dependencies;

    private ModuleBuilder(final String name, final String archiveName, final Path modulePath) {
        this.name = name;
        this.modulePath = modulePath == null ? Modules.discoverModulePath() : modulePath;
        final String resourceName = archiveName == null ? "test-module.jar" : archiveName;
        jar = ShrinkWrap.create(JavaArchive.class, resourceName);
        dependencies = new ArrayList<>();
    }

    /**
     * Creates a new module builder with an archive name of test-module.jar.
     *
     * @param moduleName the name for the module
     *
     * @return a new module builder
     */
    public static ModuleBuilder of(final String moduleName) {
        return of(moduleName, null);
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
     * Returns the module name.
     *
     * @return the module name
     */
    public String name() {
        return name;
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
        this.dependencies.add(ModuleDependency.of(dependency));
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
        Collections.addAll(this.dependencies, dependencies);
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
    public Runnable build() {
        try {
            final Path mp = modulePath;
            final Path moduleDir = mp.resolve(name.replace('.', File.separatorChar)).resolve("main");
            if (Files.notExists(moduleDir)) {
                Files.createDirectories(moduleDir);
            }
            final Path fullPathToDelete = moduleDir.subpath(0, mp.getNameCount() + 1);
            createModule(moduleDir);
            return new Runnable() {
                @Override
                public void run() {
                    try {
                        delete(moduleDir, true);
                    } catch (IOException ignore) {
                        // There is a chance resources are still locked. We will add a shutdown hook here to delete
                        // the files once the JVM has shutdown
                        final Thread task = new Thread(() -> {
                            try {
                                delete(moduleDir, true);
                            } catch (IOException e) {
                                Logger.getLogger(ModuleBuilder.class).errorf(e, "Failed to delete module %s", name);
                            }
                        }, String.format("%s-shutdown", name));
                        task.setDaemon(true);
                        Runtime.getRuntime().addShutdownHook(task);
                    }
                }

                private void delete(final Path dir, final boolean deleteFiles) throws IOException {
                    if (deleteFiles) {
                        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
                            for (Path path : dirStream) {
                                if (!Files.isDirectory(path)) {
                                    Files.delete(path);
                                }
                            }
                        }
                    }
                    if (isDirectoryEmpty(dir)) {
                        Files.delete(dir);
                    } else {
                        return;
                    }
                    final Path parent = dir.getParent();
                    if (parent != null && !parent.equals(mp)) {
                        delete(parent, false);
                    }
                }

                private boolean isDirectoryEmpty(final Path dir) throws IOException {
                    try (Stream<Path> files = Files.list(dir)) {
                        return files.findAny().isEmpty();
                    }
                }
            };
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
            writer.writeEndElement();

            // Write the dependencies
            if (!dependencies.isEmpty()) {
                writer.writeStartElement("dependencies");
                for (ModuleDependency dependency : dependencies) {
                    writer.writeEmptyElement("module");
                    writer.writeAttribute("name", dependency.getName());
                    if (dependency.isExport()) {
                        writer.writeAttribute("export", "true");
                    }
                    if (dependency.isOptional()) {
                        writer.writeAttribute("optional", "true");
                    }
                    if (dependency.getServices() != null) {
                        writer.writeAttribute("services", dependency.getServices().toString());
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

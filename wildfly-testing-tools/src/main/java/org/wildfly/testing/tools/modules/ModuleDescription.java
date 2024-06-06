/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.tools.modules;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;

import org.jboss.logging.Logger;
import org.wildfly.testing.tools.xml.CloseableXMLStreamWriter;

/**
 * Describes a created module. When this is {@linkplain #close() closed} the module will be deleted. If the files and
 * directory cannot be deleted, a {@linkplain Runtime#addShutdownHook(Thread) shutdown hook} will be added to delete the
 * module when the JVM exits. This typically happens on Windows as the module loader holds a lock on the resources.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("unused")
public final class ModuleDescription implements AutoCloseable, Comparable<ModuleDescription> {
    private final String name;
    private final Path baseModulePath;
    private final Path modulePath;

    ModuleDescription(final String name, final Path baseModulePath, final Path modulePath) {
        this.name = Objects.requireNonNull(name);
        this.baseModulePath = Objects.requireNonNull(baseModulePath);
        this.modulePath = Objects.requireNonNull(modulePath);
    }

    /**
     * Creates a module-alias with the {@linkplain Modules#discoverModulePath() discovered} module path.
     *
     * @param moduleName the name of the module
     * @param targetName the target name for the alias
     *
     * @return the module description for the alias
     */
    public static ModuleDescription createAlias(final String moduleName, final String targetName) {
        return createAlias(Modules.discoverModulePath(), moduleName, targetName);
    }

    /**
     * Creates a module alias based on the module path. If the module path is {@code null} an attempt to
     * {@linkplain Modules#discoverModulePath() discover} it will be done.
     *
     * @param modulePath the path to the base module path
     * @param moduleName the name of the module
     * @param targetName the target name for the alias
     *
     * @return the module description for the alias
     */
    public static ModuleDescription createAlias(final Path modulePath, final String moduleName, final String targetName) {
        final Path mp = modulePath == null ? Modules.discoverModulePath() : modulePath;
        final Path moduleDir = mp.resolve(moduleName.replace('.', File.separatorChar)).resolve("main");
        try {
            if (Files.notExists(moduleDir)) {
                Files.createDirectories(moduleDir);
            }
            try (
                    CloseableXMLStreamWriter writer = CloseableXMLStreamWriter
                            .of(Files.newBufferedWriter(moduleDir.resolve("module.xml")))) {
                writer.writeStartDocument("utf-8", "1.0");
                writer.writeEmptyElement("module-alias");
                writer.writeNamespace(null, "urn:jboss:module:1.9");
                writer.writeAttribute("name", moduleName);
                writer.writeAttribute("target-name", targetName);
                writer.writeEndDocument();
            } catch (XMLStreamException e) {
                throw new RuntimeException(
                        String.format("Failed to create the module-alias for %s with a target of %s", moduleName, targetName),
                        e);
            }
            return new ModuleDescription(moduleName, mp, moduleDir);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    String.format("Failed to create the module-alias for %s with a target of %s", moduleName, targetName), e);
        }
    }

    /**
     * The modules name.
     *
     * @return the modules name
     */
    public String name() {
        return name;
    }

    /**
     * The path to the module.
     *
     * @return the path to the module
     */
    public Path modulePath() {
        return modulePath;
    }

    @Override
    public int compareTo(final ModuleDescription o) {
        int result = name.compareTo(o.name);
        if (result == 0) {
            result = modulePath.compareTo(o.modulePath);
        }
        return result;
    }

    /**
     * Deletes the module files and directories. If an {@linkplain IOException error} occurs attempting to delete the
     * module, a {@linkplain Runtime#addShutdownHook(Thread) shutdown hook} is added to attempt to delete the resources
     * when the JVM exits.
     */
    @Override
    public void close() {
        try {
            delete(modulePath, true);
        } catch (IOException ignore) {
            // There is a chance resources are still locked. We will add a shutdown hook here to delete
            // the files once the JVM has shutdown
            final Thread task = new Thread(() -> {
                try {
                    delete(modulePath, true);
                } catch (IOException e) {
                    Logger.getLogger(ModuleBuilder.class).errorf(e, "Failed to delete module %s", name);
                }
            }, String.format("%s-shutdown", name));
            task.setDaemon(true);
            Runtime.getRuntime().addShutdownHook(task);
        }
    }

    @Override
    public String toString() {
        return "ModuleDescription{" + "name='" + name + '\'' + ", modulePath=" + modulePath + '}';
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModuleDescription)) {
            return false;
        }
        final ModuleDescription other = (ModuleDescription) obj;
        return Objects.equals(name, other.name) && Objects.equals(modulePath, other.modulePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, modulePath);
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
        if (parent != null && !parent.equals(baseModulePath)) {
            delete(parent, false);
        }
    }

    private boolean isDirectoryEmpty(final Path dir) throws IOException {
        try (Stream<Path> files = Files.list(dir)) {
            return files.findAny().isEmpty();
        }
    }
}

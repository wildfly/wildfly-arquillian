/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.tools.modules;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Describes a created module. When this is {@linkplain #close() closed} the module will be deleted. If the files and
 * directory cannot be deleted, a {@linkplain Runtime#addShutdownHook(Thread) shutdown hook} will be added to delete the
 * module when the JVM exits. This typically happens on Windows as the module loader holds a lock on the resources.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @deprecated use the new WildFly Testing Tools project
 */
@Deprecated(forRemoval = true, since = "6.0")
@SuppressWarnings("unused")
public final class ModuleDescription implements AutoCloseable, Comparable<ModuleDescription> {
    private final org.wildfly.testing.tools.module.ModuleDescription delegate;

    ModuleDescription(final org.wildfly.testing.tools.module.ModuleDescription delegate) {
        this.delegate = delegate;
    }

    /**
     * Creates a module-alias with the {@linkplain org.wildfly.testing.tools.module.Modules#discoverModulePath() discovered}
     * module path.
     *
     * @param moduleName the name of the module
     * @param targetName the target name for the alias
     *
     * @return the module description for the alias
     */
    public static ModuleDescription createAlias(final String moduleName, final String targetName) {
        return createAlias(org.wildfly.testing.tools.module.Modules.discoverModulePath(), moduleName, targetName);
    }

    /**
     * Creates a module alias based on the module path. If the module path is {@code null} an attempt to
     * {@linkplain org.wildfly.testing.tools.module.Modules#discoverModulePath() discover} it will be done.
     *
     * @param modulePath the path to the base module path
     * @param moduleName the name of the module
     * @param targetName the target name for the alias
     *
     * @return the module description for the alias
     */
    public static ModuleDescription createAlias(final Path modulePath, final String moduleName, final String targetName) {
        return new ModuleDescription(
                org.wildfly.testing.tools.module.ModuleDescription.createAlias(modulePath, moduleName, targetName));
    }

    /**
     * The modules name.
     *
     * @return the modules name
     */
    public String name() {
        return delegate.name();
    }

    /**
     * The path to the module.
     *
     * @return the path to the module
     */
    public Path modulePath() {
        return delegate.modulePath();
    }

    @Override
    public int compareTo(final ModuleDescription o) {
        return delegate.compareTo(o.delegate);
    }

    /**
     * Deletes the module files and directories. If an {@linkplain IOException error} occurs attempting to delete the
     * module, a {@linkplain Runtime#addShutdownHook(Thread) shutdown hook} is added to attempt to delete the resources
     * when the JVM exits.
     */
    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public String toString() {
        return "ModuleDescription{" + "name='" + name() + '\'' + ", modulePath=" + modulePath() + '}';
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final ModuleDescription other)) {
            return false;
        }
        return Objects.equals(delegate, other.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }
}

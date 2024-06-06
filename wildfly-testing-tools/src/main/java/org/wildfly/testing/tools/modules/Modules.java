/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.tools.modules;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for JBoss Modules.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class Modules {

    private static final Set<Path> IMMUTABLE_MODULE_PATHS;

    static {
        final String immutableModulePath = System.getProperty("org.wildfly.testing.tools.modules.immutable.paths");
        if (immutableModulePath != null) {
            IMMUTABLE_MODULE_PATHS = Stream.of(immutableModulePath.split(File.pathSeparator))
                    .map(Path::of)
                    .collect(Collectors.toSet());
        } else {
            IMMUTABLE_MODULE_PATHS = Set.of();
        }
    }

    /**
     * Discovers the JBoss Module directory to use. Use the {@code org.wildfly.testing.tools.modules.immutable.paths}
     * system property to add a list of paths, separated by a {@linkplain File#pathSeparatorChar path separator}, which
     * are immutable. The first non-immutable path is returned.
     *
     * <p>
     * Paths may be immutable for testing due to not wanting to pollute a local installation of the server.
     * </p>
     *
     * @return the JBoss Modules path where modules can be stored
     *
     * @throws IllegalStateException if there is no module path found or the only module paths are immutable paths
     */
    public static Path discoverModulePath() {
        String modulePath = System.getProperty("module.path");

        final Path moduleDir;

        if (modulePath == null) {
            String jbossHome = System.getProperty("jboss.home", System.getenv("JBOSS_HOME"));

            if (jbossHome == null) {
                throw new IllegalStateException("Neither -Dmodule.path nor -Djboss.home were set");
            }

            moduleDir = Path.of(jbossHome, "modules");

            if (isImmutableModulePath(moduleDir)) {
                throw new IllegalStateException(
                        String.format("Writing test modules in jboss.home directory %s is not allowed.", jbossHome));
            }
        } else {
            String pathElement = null;
            for (String candidate : modulePath.split(File.pathSeparator)) {
                if (!isImmutableModulePath(candidate)) {
                    pathElement = candidate;
                    break;
                }
            }
            if (pathElement == null) {
                throw new IllegalStateException(
                        String.format("Writing test modules in module.path directories %s is not allowed.", modulePath));
            }
            moduleDir = Path.of(pathElement);
        }
        if (Files.notExists(moduleDir) || !Files.isDirectory(moduleDir)) {
            throw new IllegalStateException(String.format("Module directory %s is not a directory.", moduleDir));
        }

        return moduleDir;
    }

    private static boolean isImmutableModulePath(final Path path) {
        for (Path immutableModulePath : IMMUTABLE_MODULE_PATHS) {
            if (contains(path, immutableModulePath)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isImmutableModulePath(final String path) {
        final Path checking = Path.of(path);
        for (Path immutableModulePath : IMMUTABLE_MODULE_PATHS) {
            if (contains(checking, immutableModulePath)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(final Path p1, final Path p2) {
        // If the paths are equal, we can say it's a match
        if (p1.equals(p2)) {
            return true;
        }
        // If the path to check is smaller than the path we're checking, they cannot match
        if (p1.getNameCount() < p2.getNameCount()) {
            return false;
        }

        // Scan the path segments until we have a match
        int start = -1;
        for (Path segment : p1) {
            start++;
            if (segment.equals(p2.getName(0))) {
                break;
            }
        }
        final int end = start + p2.getNameCount();
        if (end > p1.getNameCount()) {
            return false;
        }
        return p1.subpath(start, end).equals(p2);
    }
}

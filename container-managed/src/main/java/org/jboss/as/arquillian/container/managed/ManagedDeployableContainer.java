/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.jboss.as.arquillian.container.CommonManagedDeployableContainer;
import org.jboss.as.arquillian.container.ParameterUtils;
import org.jboss.logging.Logger;
import org.wildfly.core.launcher.CommandBuilder;
import org.wildfly.core.launcher.StandaloneCommandBuilder;

/**
 * The managed deployable container.
 *
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @since 17-Nov-2010
 */
public final class ManagedDeployableContainer extends CommonManagedDeployableContainer<ManagedContainerConfiguration> {
    private static final Pattern WHITESPACE_OR_COMMA_DELIMITED = Pattern.compile("(\\s+|,)(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

    static final String TEMP_CONTAINER_DIRECTORY = "arquillian-temp-container";

    static final String CONFIG_DIR = "configuration";
    static final String DATA_DIR = "data";

    private final Logger log = Logger.getLogger(ManagedDeployableContainer.class);

    @Override
    public Class<ManagedContainerConfiguration> getConfigurationClass() {
        return ManagedContainerConfiguration.class;
    }

    @Override
    protected CommandBuilder createCommandBuilder(ManagedContainerConfiguration config) {
        final StandaloneCommandBuilder commandBuilder = StandaloneCommandBuilder.of(config.getJbossHome());

        String modulesPath = config.getModulePath();
        if (modulesPath != null && !modulesPath.isEmpty()) {
            commandBuilder.setModuleDirs(modulesPath.split(Pattern.quote(File.pathSeparator)));
        }

        @SuppressWarnings("deprecation")
        String bundlesPath = config.getBundlePath();
        if (bundlesPath != null && !bundlesPath.isEmpty()) {
            getLogger().warn("Bundles path is deprecated and no longer used.");
        }

        final String javaOpts = config.getJavaVmArguments();
        final String jbossArguments = config.getJbossArguments();

        commandBuilder.setJavaHome(config.getJavaHome());
        if (javaOpts != null && !javaOpts.trim().isEmpty()) {
            commandBuilder.setJavaOptions(ParameterUtils.splitParams(javaOpts));
        }

        final String moduleOptions = config.getModuleOptions();
        if (moduleOptions != null && !moduleOptions.isBlank()) {
            commandBuilder.setModuleOptions(ParameterUtils.splitParams(moduleOptions));
        }

        if (config.isEnableAssertions()) {
            commandBuilder.addJavaOption("-ea");
        }

        if (config.isAdminOnly()) {
            commandBuilder.setAdminOnly();
        }

        if (jbossArguments != null && !jbossArguments.trim().isEmpty()) {
            commandBuilder.addServerArguments(ParameterUtils.splitParams(jbossArguments));
        }

        // Only one of the two should be set. The configuration will validate this, but we should only set one
        if (config.getServerConfig() != null) {
            commandBuilder.setServerConfiguration(config.getServerConfig());
        } else if (config.getReadOnlyServerConfig() != null) {
            commandBuilder.setServerReadOnlyConfiguration(config.getReadOnlyServerConfig());
        }

        if (config.getYamlConfiguration() != null) {
            commandBuilder.setYamlFiles(findSupplementalConfigurationFiles(commandBuilder.getConfigurationDirectory(),
                    config.getYamlConfiguration()));
        }

        // Create a clean server base to run the container; ARQ-638
        if (config.isSetupCleanServerBaseDir() || config.getCleanServerBaseDir() != null) {
            try {
                setupCleanServerDirectories(commandBuilder, config.getCleanServerBaseDir());
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to setup the clean server directory.", e);
            }
        }

        // Check if we should enable debug
        if (config.isDebug()) {
            commandBuilder.setDebug(config.isDebugSuspend(), config.getDebugPort());
        }

        // Previous versions of arquillian set the jboss.home.dir property in the JVM properties.
        // Some tests may rely on this behavior, but could be considered to be removed as all the scripts add this
        // property after the modules path (-mp) has been defined. The command builder will set the property after
        // the module path has been defined as well.
        commandBuilder.addJavaOption("-Djboss.home.dir=" + commandBuilder.getWildFlyHome());
        return commandBuilder;
    }

    private Path[] findSupplementalConfigurationFiles(final Path serverConfigurationDirPath, final String yaml) {
        final Collection<Path> yamlFiles = new ArrayList<>();
        // Validate the paths exist
        for (var yamlFile : WHITESPACE_OR_COMMA_DELIMITED.split(yaml)) {
            var path = Path.of(yamlFile);
            if (path.isAbsolute()) {
                yamlFiles.add(path);
            } else {
                yamlFiles.add(serverConfigurationDirPath.resolve(path));
            }
        }
        // Validate the YAML files before we return them
        final Collection<Path> invalidPaths = new ArrayList<>();
        for (var yamlFile : yamlFiles) {
            if (Files.notExists(yamlFile)) {
                invalidPaths.add(yamlFile);
            }
        }
        if (!invalidPaths.isEmpty()) {
            throw new IllegalStateException(String.format("Invalid YAML paths found in %s: %s", yaml, invalidPaths));
        }
        return yamlFiles.toArray(new Path[0]);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * Setup clean directories to run the container.
     *
     * @param cleanServerBaseDirPath the clean server base directory
     */
    private void setupCleanServerDirectories(final StandaloneCommandBuilder commandBuilder,
            final String cleanServerBaseDirPath) throws IOException {
        final Path cleanBase;
        if (cleanServerBaseDirPath != null) {
            cleanBase = Paths.get(cleanServerBaseDirPath);
            if (Files.exists(cleanBase)) {
                if (!deleteDir(cleanBase)) {
                    log.warnf("Clean directory %s was not empty when copied. Previous data will be lost.", cleanBase);
                }
            }
            Files.createDirectories(cleanBase);
        } else {
            cleanBase = Files.createTempDirectory(TEMP_CONTAINER_DIRECTORY);
        }

        if (Files.notExists(cleanBase)) {
            throw new IllegalStateException(String.format("Base directory %s does not exist.", cleanBase));
        }
        if (!Files.isDirectory(cleanBase)) {
            throw new IllegalStateException(String.format("Base directory %s is not a directory.", cleanBase));
        }

        final Path currentConfigDir = commandBuilder.getConfigurationDirectory();
        final Path configDir = cleanBase.resolve(CONFIG_DIR);
        copyDir(currentConfigDir, configDir);

        final Path currentDataDir = commandBuilder.getBaseDirectory().resolve(DATA_DIR);
        if (Files.exists(currentDataDir)) {
            copyDir(currentDataDir, cleanBase.resolve(DATA_DIR));
        }
        commandBuilder.setBaseDirectory(cleanBase);
        commandBuilder.setConfigurationDirectory(configDir);
    }

    private static void copyDir(final Path from, final Path to) throws IOException {
        Files.walkFileTree(from, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                Files.copy(dir, to.resolve(from.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.copy(file, to.resolve(from.relativize(file)));
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static boolean deleteDir(final Path toDelete) throws IOException {
        final AtomicBoolean empty = new AtomicBoolean(true);
        Files.walkFileTree(toDelete, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                Files.delete(dir);
                if (!dir.equals(toDelete)) {
                    empty.compareAndSet(true, false);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                empty.compareAndSet(true, false);
                return FileVisitResult.CONTINUE;
            }
        });
        return empty.get();
    }
}

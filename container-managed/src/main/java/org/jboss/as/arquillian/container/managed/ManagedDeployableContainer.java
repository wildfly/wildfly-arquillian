/*
 * Copyright 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import java.util.regex.Pattern;

import org.jboss.as.arquillian.container.CommonManagedDeployableContainer;
import org.jboss.as.arquillian.container.ParameterUtils;
import org.jboss.as.server.logging.ServerLogger;
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

    static final String TEMP_CONTAINER_DIRECTORY = "arquillian-temp-container";

    static final String CONFIG_DIR = "configuration";
    static final String DATA_DIR = "data";

    private final Logger log = Logger.getLogger(ManagedDeployableContainer.class);

    @Override
    public Class<ManagedContainerConfiguration> getConfigurationClass() {
        return ManagedContainerConfiguration.class;
    }

    @Override
    @SuppressWarnings("FeatureEnvy")
    protected CommandBuilder createCommandBuilder(final ManagedContainerConfiguration config) {
        final StandaloneCommandBuilder commandBuilder = StandaloneCommandBuilder.of(config.getJbossHome());

        String modulesPath = config.getModulePath();
        if (modulesPath != null && !modulesPath.isEmpty()) {
            commandBuilder.setModuleDirs(modulesPath.split(Pattern.quote(File.pathSeparator)));
        }

        @SuppressWarnings("deprecation")
        String bundlesPath = config.getBundlePath();
        if (bundlesPath != null && !bundlesPath.isEmpty()) {
            log.warn("Bundles path is deprecated and no longer used.");
        }

        final String javaOpts = config.getJavaVmArguments();
        final String jbossArguments = config.getJbossArguments();

        commandBuilder.setJavaHome(config.getJavaHome());
        if (javaOpts != null && !javaOpts.trim().isEmpty()) {
            commandBuilder.setJavaOptions(ParameterUtils.splitParams(javaOpts));
        }

        final String moduleOptions = config.getModuleOptions();
        if (moduleOptions != null && !moduleOptions.trim().isEmpty()) {
            commandBuilder.setModuleOptions(ParameterUtils.splitParams(moduleOptions));
        }

        if (config.isEnableAssertions()) {
            commandBuilder.addJavaOption("-ea");
        }

        if (config.isAdminOnly())
            commandBuilder.setAdminOnly();

        if (jbossArguments != null && !jbossArguments.trim().isEmpty()) {
            commandBuilder.addServerArguments(ParameterUtils.splitParams(jbossArguments));
        }

        // Only one of the two should be set. The configuration will validate this, but we should only set one
        if (config.getServerConfig() != null) {
            commandBuilder.setServerConfiguration(config.getServerConfig());
        } else if (config.getReadOnlyServerConfig() != null) {
            commandBuilder.setServerReadOnlyConfiguration(config.getReadOnlyServerConfig());
        }

        // Create a clean server base to run the container; ARQ-638
        if (config.isSetupCleanServerBaseDir() || config.getCleanServerBaseDir() != null) {
            try {
                setupCleanServerDirectories(commandBuilder, config.getCleanServerBaseDir());
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to setup the clean server directory.", e);
            }
        }

        // Previous versions of arquillian set the jboss.home.dir property in the JVM properties.
        // Some tests may rely on this behavior, but could be considered to be removed as all the scripts add this
        // property after the modules path (-mp) has been defined. The command builder will set the property after
        // the module path has been defined as well.
        commandBuilder.addJavaOption("-Djboss.home.dir=" + commandBuilder.getWildFlyHome());
        return commandBuilder;
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
    private static void setupCleanServerDirectories(final StandaloneCommandBuilder commandBuilder,
            final String cleanServerBaseDirPath) throws IOException {
        final Path cleanBase;
        if (cleanServerBaseDirPath != null) {
            cleanBase = Paths.get(cleanServerBaseDirPath);
        } else {
            cleanBase = Files.createTempDirectory(TEMP_CONTAINER_DIRECTORY);
        }

        if (Files.notExists(cleanBase)) {
            throw ServerLogger.ROOT_LOGGER.serverBaseDirectoryDoesNotExist(cleanBase.toFile());
        }
        if (!Files.isDirectory(cleanBase)) {
            throw ServerLogger.ROOT_LOGGER.serverBaseDirectoryIsNotADirectory(cleanBase.toFile());
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
        Files.walkFileTree(from, new SimpleFileVisitor<Path>() {
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
}

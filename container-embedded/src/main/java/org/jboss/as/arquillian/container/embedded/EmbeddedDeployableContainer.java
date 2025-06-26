/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.embedded;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.as.arquillian.container.CommonDeployableContainer;
import org.jboss.as.arquillian.container.ParameterUtils;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleLoader;
import org.wildfly.core.embedded.Configuration;
import org.wildfly.core.embedded.EmbeddedProcessFactory;
import org.wildfly.core.embedded.EmbeddedStandaloneServerFactory;
import org.wildfly.core.embedded.StandaloneServer;

/**
 * {@link org.jboss.arquillian.container.spi.client.container.DeployableContainer} implementation to bootstrap JBoss Logging
 * (installing the LogManager if possible), use the JBoss
 * Modules modular ClassLoading Environment to create a new server instance, and handle lifecycle of the Application Server in
 * the currently-running environment.
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 * @author <a href="mailto:mmatloka@gmail.com">Michal Matloka</a>
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class EmbeddedDeployableContainer extends CommonDeployableContainer<EmbeddedContainerConfiguration> {
    private static final String JAVA_CLASS_PATH = "java.class.path";

    /**
     * Hook to the server; used in start/stop, created by setup
     */
    private StandaloneServer server;

    @Override
    public void setup(final EmbeddedContainerConfiguration config) {
        super.setup(config);
        if (config.getCleanServerBaseDir() != null) {
            SecurityActions.setSystemProperty(EmbeddedStandaloneServerFactory.JBOSS_EMBEDDED_ROOT,
                    config.getCleanServerBaseDir());
        }
        final String[] cmdArgs = getCommandArgs(config);
        final Configuration configuration = Configuration.Builder.of(Path.of(config.getJbossHome()))
                .setCommandArguments(cmdArgs)
                .setModuleLoader(setupModuleLoader(config.getModulePath(), config.getSystemPackagesArray()))
                .build();
        server = EmbeddedProcessFactory.createStandaloneServer(configuration);
    }

    @Override
    public Class<EmbeddedContainerConfiguration> getConfigurationClass() {
        return EmbeddedContainerConfiguration.class;
    }

    @Override
    protected void startInternal() throws LifecycleException {
        try {
            server.start();
        } catch (Throwable e) {
            throw new LifecycleException("Could not invoke start on: " + server, e);
        }
    }

    @Override
    protected void stopInternal(Integer timeout) throws LifecycleException {
        try {
            // Timeout is ignored in the embeddable case.
            server.stop();
        } catch (Throwable e) {
            throw new LifecycleException("Could not invoke stop on: " + server, e);
        }
    }

    private static String[] getCommandArgs(final EmbeddedContainerConfiguration config) {
        final String configFile = config.getServerConfig();
        final String arguments = config.getJbossArguments();
        if (arguments == null) {
            if (configFile == null) {
                return new String[0];
            }
            return new String[] { "-c=" + configFile };
        }
        List<String> splitParams = ParameterUtils.splitParams(arguments);
        if (configFile != null) {
            splitParams.add("-c=" + configFile);
        }
        return splitParams.toArray(new String[0]);
    }

    private static ModuleLoader setupModuleLoader(final String modulePath, final String... systemPackages) {

        assert modulePath != null : "modulePath not null";

        final Path moduleDir = Paths.get(trimPathToModulesDir(modulePath));
        if (Files.notExists(moduleDir) || !Files.isDirectory(moduleDir)) {
            throw new RuntimeException(
                    "The first directory of the specified module path " + modulePath + " is invalid or does not exist.");
        }

        SecurityActions.setSystemProperty("jboss.modules.dir", moduleDir.toAbsolutePath()
                .toString());

        final String classPath = SecurityActions.getSystemProperty(JAVA_CLASS_PATH);
        try {
            // Clear the java.class.path and setup the module.path
            SecurityActions.clearSystemProperty(JAVA_CLASS_PATH);
            SecurityActions.setSystemProperty("module.path", modulePath);

            // The default system packages should only be org.jboss.modules, other packages should come from modules.
            final StringBuilder packages = new StringBuilder("org.jboss.modules");
            if (systemPackages != null) {
                for (String packageName : systemPackages) {
                    packages.append(",");
                    packages.append(packageName);
                }
            }
            SecurityActions.setSystemProperty("jboss.modules.system.pkgs", packages.toString());

            // Get the module loader
            return Module.getBootModuleLoader();
        } finally {
            // Return to previous state for classpath prop
            if (classPath != null) {
                SecurityActions.setSystemProperty(JAVA_CLASS_PATH, classPath);
            }
        }
    }

    private static String trimPathToModulesDir(String modulePath) {
        int index = modulePath.indexOf(File.pathSeparator);
        return index == -1 ? modulePath : modulePath.substring(0, index);
    }
}

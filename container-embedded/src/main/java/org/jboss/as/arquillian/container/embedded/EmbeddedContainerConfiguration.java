/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.embedded;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.deployment.Validate;
import org.jboss.as.arquillian.container.CommonContainerConfiguration;
import org.wildfly.core.embedded.EmbeddedStandaloneServerFactory;

/**
 * {@link org.jboss.arquillian.container.spi.client.container.ContainerConfiguration} implementation for JBoss AS Embedded
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 * @author <a href="mailto:mmatloka@gmail.com">Michal Matloka</a>
 */
public class EmbeddedContainerConfiguration extends CommonContainerConfiguration {

    private String jbossHome = System.getenv("JBOSS_HOME");

    private String modulePath = System.getProperty("module.path");

    private String cleanServerBaseDir = System.getProperty(EmbeddedStandaloneServerFactory.JBOSS_EMBEDDED_ROOT);

    private String jbossArguments;

    private String serverConfig = System.getProperty("jboss.server.config.file.name");

    private String systemPackages = System.getProperty("jboss.modules.system.pkgs");

    public EmbeddedContainerConfiguration() {

        // if no jbossHome is set use jboss.home of already running jvm
        if (jbossHome == null || jbossHome.isEmpty()) {
            jbossHome = System.getProperty("jboss.home");
        }

        if ((modulePath == null || modulePath.isEmpty()) && jbossHome != null) {
            modulePath = jbossHome + "/modules";
        }
    }

    /**
     * @return the jbossHome
     */
    public String getJbossHome() {
        return jbossHome;
    }

    /**
     * @param jbossHome the jbossHome to set
     */
    public void setJbossHome(String jbossHome) {
        this.jbossHome = jbossHome;
    }

    public String getModulePath() {
        return modulePath;
    }

    public void setModulePath(final String modulePath) {
        this.modulePath = modulePath;
    }

    public String getCleanServerBaseDir() {
        return cleanServerBaseDir;
    }

    public void setCleanServerBaseDir(String cleanServerBaseDir) {
        this.cleanServerBaseDir = cleanServerBaseDir;
    }

    public String getJbossArguments() {
        return jbossArguments;
    }

    public void setJbossArguments(final String jbossArguments) {
        this.jbossArguments = jbossArguments;
    }

    public String getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(final String serverConfig) {
        this.serverConfig = serverConfig;
    }

    public String getSystemPackages() {
        return systemPackages;
    }

    public void setSystemPackages(String systemPackages) {
        this.systemPackages = systemPackages;
    }

    public String[] getSystemPackagesArray() {
        return (systemPackages == null || systemPackages.isEmpty()) ? null : systemPackages.split(",");
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.as.arquillian.container.CommonContainerConfiguration#validate()
     */
    @Override
    public void validate() throws ConfigurationException {
        super.validate();
        Validate.configurationDirectoryExists(jbossHome, "jbossHome '" + jbossHome + "' must exist");
        Validate.configurationDirectoryExists(modulePath, "modulePath '" + modulePath + "' must exist");
    }
}

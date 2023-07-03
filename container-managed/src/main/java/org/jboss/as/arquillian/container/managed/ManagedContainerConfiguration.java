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

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.as.arquillian.container.DistributionContainerConfiguration;

/**
 * The managed container configuration
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author Thomas.Diesler@jboss.com
 */
public class ManagedContainerConfiguration extends DistributionContainerConfiguration {

    private String javaVmArguments = System.getProperty("jboss.options");

    private String jbossArguments;

    private String moduleOptions;

    private String serverConfig = System.getProperty("jboss.server.config.file.name");

    private String readOnlyServerConfig = System.getProperty("jboss.server.config.file.name.readonly");

    private boolean enableAssertions = true;

    private boolean adminOnly = false;

    private boolean setupCleanServerBaseDir = false;

    private String cleanServerBaseDir;

    public ManagedContainerConfiguration() {
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();
        // Cannot define both a serverConfig and a readOnlyServerConfig
        if (serverConfig != null && readOnlyServerConfig != null) {
            throw new ConfigurationException(String.format("Cannot define both a serverConfig and a readOnlyServerConfig: " +
                    "serverConfig=%s - readOnlyServerConfig=%s", serverConfig, readOnlyServerConfig));
        }
    }

    public String getJavaVmArguments() {
        return javaVmArguments;
    }

    public void setJavaVmArguments(String javaVmArguments) {
        this.javaVmArguments = javaVmArguments;
    }

    public String getJbossArguments() {
        return jbossArguments;
    }

    public void setJbossArguments(String jbossArguments) {
        this.jbossArguments = jbossArguments;
    }

    public String getModuleOptions() {
        return moduleOptions;
    }

    public void setModuleOptions(String moduleOptions) {
        this.moduleOptions = moduleOptions;
    }

    /**
     * Get the server configuration file name. Equivalent to [-server-config=...] on the command line.
     */
    public String getServerConfig() {
        return serverConfig;
    }

    /**
     * Set the server configuration file name. Equivalent to [-server-config=...] on the command line.
     */
    public void setServerConfig(String serverConfig) {
        this.serverConfig = serverConfig;
    }

    /**
     * Get the server configuration file name. Equivalent to [--read-only-server-config=...] on the command line.
     */
    public String getReadOnlyServerConfig() {
        return readOnlyServerConfig;
    }

    /**
     * Set the server configuration file name. Equivalent to [--read-only-server-config=...] on the command line.
     */
    public void setReadOnlyServerConfig(String serverConfig) {
        this.readOnlyServerConfig = serverConfig;
    }

    public boolean isEnableAssertions() {
        return enableAssertions;
    }

    public void setEnableAssertions(final boolean enableAssertions) {
        this.enableAssertions = enableAssertions;
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }

    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    public boolean isSetupCleanServerBaseDir() {
        return setupCleanServerBaseDir;
    }

    public void setSetupCleanServerBaseDir(boolean setupCleanServerBaseDir) {
        this.setupCleanServerBaseDir = setupCleanServerBaseDir;
    }

    public String getCleanServerBaseDir() {
        return cleanServerBaseDir;
    }

    public void setCleanServerBaseDir(String cleanServerBaseDir) {
        this.cleanServerBaseDir = cleanServerBaseDir;
    }
}

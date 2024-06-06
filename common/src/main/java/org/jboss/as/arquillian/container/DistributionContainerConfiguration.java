/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.deployment.Validate;

/**
 * Container configuration for containers backed by a distribution installation (ie. Remote and Embedded)
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class DistributionContainerConfiguration extends CommonManagedContainerConfiguration {

    private String jbossHome = System.getenv("JBOSS_HOME");

    private String modulePath = System.getProperty("module.path");

    private String bundlePath = System.getProperty("bundle.path");

    public DistributionContainerConfiguration() {
        // if no jbossHome is set use jboss.home of already running jvm
        if (jbossHome == null || jbossHome.isEmpty()) {
            jbossHome = System.getProperty("jboss.home");
        }
    }

    /**
     * @return the jbossHome
     */
    public String getJbossHome() {
        return jbossHome;
    }

    /**
     * @param jbossHome
     *                      the jbossHome to set
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

    @Deprecated
    public String getBundlePath() {
        return bundlePath;
    }

    @Deprecated
    public void setBundlePath(String bundlePath) {
        this.bundlePath = bundlePath;
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();
        if (jbossHome != null)
            Validate.configurationDirectoryExists(jbossHome, "jbossHome '" + jbossHome + "' must exist");
    }
}

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

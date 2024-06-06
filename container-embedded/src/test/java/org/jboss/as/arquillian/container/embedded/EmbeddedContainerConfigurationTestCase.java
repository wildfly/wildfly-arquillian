/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.embedded;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.junit.Test;

/**
 * @author <a href="mailto:tommy.tynja@diabol.se">Tommy Tynj&auml;</a>
 */
public class EmbeddedContainerConfigurationTestCase {

    @Test
    public void shouldValidateDefaultConfiguration() {
        final EmbeddedContainerConfiguration conf = new EmbeddedContainerConfiguration();
        conf.validate();
    }

    @Test(expected = ConfigurationException.class)
    public void shouldValidateThatModulePathIsNonExisting() {
        final EmbeddedContainerConfiguration conf = new EmbeddedContainerConfiguration();
        conf.setModulePath("");
        validate(conf);
    }

    @Test
    public void shouldNotValidateBundlePathIfNonExisting() {
        final EmbeddedContainerConfiguration conf = new EmbeddedContainerConfiguration();
        validate(conf);
    }

    @Test
    public void shouldValidateBundlePathIfExisting() {
        final EmbeddedContainerConfiguration conf = new EmbeddedContainerConfiguration();
        validate(conf);
    }

    @Test(expected = ConfigurationException.class)
    public void shouldValidateThatJbossHomePathIsNonExisting() {
        final EmbeddedContainerConfiguration conf = new EmbeddedContainerConfiguration();
        conf.setJbossHome(null);
        conf.validate();
    }

    @Test
    public void shouldValidateThatModulePathAndBundlePathExists() {
        final EmbeddedContainerConfiguration conf = new EmbeddedContainerConfiguration();
        createDir(conf.getModulePath());
        validate(conf);
    }

    private void validate(final EmbeddedContainerConfiguration conf) {
        assertNotNull(conf.getJbossHome());
        assertNotNull(conf.getModulePath());
        conf.validate();
    }

    private static void createDir(final String path) {
        if (path != null) {
            File dir = new File(path);
            if (!dir.exists()) {
                assertTrue("Failed to create directory", dir.mkdirs());
            }
        }
    }
}

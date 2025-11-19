/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CommonContainerConfiguration}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class CommonContainerConfigurationTestCase {

    @Test
    public void validDeploymentFailurePattern() {
        final CommonContainerConfiguration config = new CommonContainerConfiguration();
        config.setDeploymentFailurePattern(".*error.*");
        Assertions.assertDoesNotThrow(config::validate, "Valid regex pattern should not throw exception");
    }

    @Test
    public void nullDeploymentFailurePattern() {
        final CommonContainerConfiguration config = new CommonContainerConfiguration();
        config.setDeploymentFailurePattern(null);
        Assertions.assertDoesNotThrow(config::validate, "Null pattern should be allowed");
    }

    @Test
    public void blankDeploymentFailurePattern() {
        final CommonContainerConfiguration config = new CommonContainerConfiguration();
        config.setDeploymentFailurePattern("   ");
        Assertions.assertDoesNotThrow(config::validate, "Blank pattern should be allowed");
    }

    @Test
    public void emptyDeploymentFailurePattern() {
        final CommonContainerConfiguration config = new CommonContainerConfiguration();
        config.setDeploymentFailurePattern("");
        Assertions.assertDoesNotThrow(config::validate, "Empty pattern should be allowed");
    }

    @Test
    public void invalidDeploymentFailurePattern() {
        final CommonContainerConfiguration config = new CommonContainerConfiguration();
        // Invalid regex: unclosed character class
        config.setDeploymentFailurePattern("[invalid");
        final ConfigurationException exception = Assertions.assertThrows(ConfigurationException.class, config::validate,
                "Invalid regex pattern should throw ConfigurationException");
        Assertions.assertTrue(exception.getMessage().contains("Invalid deploymentFailurePattern regex"),
                "Exception message should mention invalid pattern: " + exception.getMessage());
    }

    @Test
    public void complexValidPattern() {
        final CommonContainerConfiguration config = new CommonContainerConfiguration();
        // Test various regex features
        config.setDeploymentFailurePattern("javax\\.xml\\.stream\\.XMLStreamException|org\\.jboss\\..*Error");
        Assertions.assertDoesNotThrow(config::validate, "Complex valid regex pattern should not throw exception");
    }

    @Test
    public void patternWithSpecialCharacters() {
        final CommonContainerConfiguration config = new CommonContainerConfiguration();
        config.setDeploymentFailurePattern("\\d+\\.\\d+\\.\\d+");
        Assertions.assertDoesNotThrow(config::validate, "Pattern with escaped special chars should be valid");
    }
}
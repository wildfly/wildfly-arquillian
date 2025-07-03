/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.jboss.arquillian.container.spi.client.deployment.Validate;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * A utility which creates and retrieves the required configuration for resource providers.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class WildFlyArquillianConfiguration {
    private static final String CONFIG_FILE = "wildfly-arquillian-config.properties";

    /**
     * Adds the file {@code wildfly-arquillian-config.properties} to the deployment.
     *
     * @param config  the configuration used to get the container properties from
     * @param archive the deployment to add the file to
     */
    public static void addConfiguration(final Map<String, String> config, final Archive<?> archive) {
        // A configuration file was already added. We will not add a new one or append to the current one
        if (archive.get(CONFIG_FILE) != null) {
            return;
        }
        // Create properties to later be retrieved to create a client
        final Properties properties = new Properties();
        properties.setProperty("management.protocol", config.getOrDefault("managementProtocol", "remote+http"));
        properties.setProperty("management.address", config.getOrDefault("managementAddress", "localhost"));
        properties.setProperty("management.port", config.getOrDefault("managementPort", "9990"));

        final String username = config.get("username");
        if (username != null) {
            properties.setProperty("username", username);
        }
        final String password = config.get("password");
        if (password != null) {
            properties.setProperty("password", password);
        }
        final String authenticationConfig = config.get("authenticationConfig");
        if (authenticationConfig != null) {
            properties.setProperty("authenticationConfig", authenticationConfig);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            properties.store(out, "");
            final Asset asset = new ByteArrayAsset(out.toByteArray());
            // We want to make sure the file ends up on the class path
            if (Validate.isArchiveOfType(WebArchive.class, archive)) {
                archive.add(asset, "WEB-INF/classes/" + CONFIG_FILE);
            } else {
                archive.add(asset, CONFIG_FILE);
            }

        } catch (IOException e) {
            // This likely won't happen, but we will re-throw as a RuntimeException
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Returns the previously configured {@code wildfly-arquillian-config.properties} from the deployments class
     * path
     *
     * @return the properties if found
     */
    public static Optional<Properties> getConfiguration() {
        final ClassLoader classLoader = getClassLoader();
        final URL resourceUrl = classLoader.getResource(CONFIG_FILE);
        if (resourceUrl != null) {
            try (InputStream in = resourceUrl.openStream()) {
                final Properties properties = new Properties();
                properties.load(in);
                return Optional.of(properties);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return Optional.empty();
    }

    private static ClassLoader getClassLoader() {
        if (System.getSecurityManager() == null) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = WildFlyArquillianConfiguration.class.getClassLoader();
            }
            return classLoader;
        }
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = WildFlyArquillianConfiguration.class.getClassLoader();
            }
            return classLoader;
        });
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container;

import static org.jboss.as.arquillian.container.Authentication.getCallbackHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.ModelControllerClientConfiguration;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;

/**
 * A provider and observer for allowing a {@link ManagementClient} to be usable inside a container for testing.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class InContainerManagementClientProvider implements ResourceProvider {
    private static final String CONFIG_FILE = "META-INF/wildfly-arquillian-config.properties";

    private static final Lock lock = new ReentrantLock();
    private static ManagementClient current;

    @Override
    public boolean canProvide(final Class<?> type) {
        return type.isAssignableFrom(ManagementClient.class);
    }

    @Override
    @SuppressWarnings("deprecation")
    public Object lookup(final ArquillianResource arquillianResource, final Annotation... annotations) {
        lock.lock();
        try {
            if (current != null) {
                return current;
            }
            final URL resourceUrl = getClass().getClassLoader().getResource(CONFIG_FILE);
            if (resourceUrl != null) {
                final String protocol;
                final String address;
                final int port;
                try (InputStream in = resourceUrl.openStream()) {
                    final Properties properties = new Properties();
                    properties.load(in);
                    protocol = properties.getProperty("management.protocol", "remote+http");
                    address = properties.getProperty("management.address", "localhost");
                    port = Integer.parseInt(properties.getProperty("management.port", "9990"));

                    // Configure a client for in-container tests based on the config data within the deployment
                    final ModelControllerClientConfiguration.Builder builder = new ModelControllerClientConfiguration.Builder()
                            .setHostName(address)
                            .setPort(port)
                            .setProtocol(protocol);

                    Authentication.username = properties.getProperty("username", "");
                    Authentication.password = properties.getProperty("password", "");

                    if (Authentication.username != null && !Authentication.username.isEmpty()) {
                        builder.setHandler(getCallbackHandler());
                    }

                    final String authenticationConfig = properties.getProperty("authenticationConfig");
                    if (authenticationConfig != null) {
                        builder.setAuthenticationConfigUri(URI.create(authenticationConfig));
                    }
                    current = new ManagementClient(ModelControllerClient.Factory.create(builder.build()), address,
                            port,
                            protocol);

                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
            return current;
        } finally {
            lock.unlock();
        }
    }

    public void addConfiguration(@Observes final BeforeDeploy event, final Container<?> container) {
        final Archive<?> archive = event.getDeployment().getArchive();
        // A configuration file was already added. We will not add a new one or append to the current one
        if (archive.get(CONFIG_FILE) != null) {
            return;
        }
        // Create properties to later be retrieved to create a client
        final Properties properties = new Properties();
        // Get the configuration for the container
        final var config = container.getContainerConfiguration().getContainerProperties();
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
            // Append the configuration file to the deployment
            archive.add(new ByteArrayAsset(out.toByteArray()), CONFIG_FILE);
        } catch (IOException e) {
            // This likely won't happen, but we will re-throw as a RuntimeException
            throw new UncheckedIOException(e);
        }
    }

    public void cleanUp(@Observes AfterSuite afterSuite) {
        lock.lock();
        try {
            if (current != null) {
                current.close();
                current = null;
            }
        } finally {
            lock.unlock();
        }
    }
}

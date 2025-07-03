/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container;

import static org.jboss.as.arquillian.container.Authentication.getCallbackHandler;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.test.spi.event.suite.AfterSuite;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.ModelControllerClientConfiguration;

/**
 * A provider and observer for allowing a {@link ManagementClient} to be usable inside a container for testing.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class InContainerManagementClientProvider implements ResourceProvider {

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
            WildFlyArquillianConfiguration.getConfiguration().ifPresent(properties -> {
                final String protocol;
                final String address;
                final int port;
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

                if (!Authentication.username.isEmpty()) {
                    builder.setHandler(getCallbackHandler());
                }

                final String authenticationConfig = properties.getProperty("authenticationConfig");
                if (authenticationConfig != null) {
                    builder.setAuthenticationConfigUri(URI.create(authenticationConfig));
                }
                current = new ManagementClient(ModelControllerClient.Factory.create(builder.build()), address,
                        port,
                        protocol);
            });
            return current;
        } finally {
            lock.unlock();
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

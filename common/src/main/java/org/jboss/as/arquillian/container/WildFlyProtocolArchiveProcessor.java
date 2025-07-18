/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container;

import java.util.Map;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.arquillian.container.test.spi.client.deployment.ProtocolArchiveProcessor;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;

/**
 * Appends the WildFly Arquillian Configuration to the deployment archive.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class WildFlyProtocolArchiveProcessor implements ProtocolArchiveProcessor {
    private static final Logger LOGGER = Logger.getLogger(WildFlyProtocolArchiveProcessor.class);

    @Inject
    private Instance<ContainerRegistry> containerRegistry;

    @Override
    public void process(final TestDeployment testDeployment, final Archive<?> protocolArchive) {
        final Container<?> container = findContainer(testDeployment.getTargetDescription().getName());
        if (container == null) {
            // We couldn't find the container, log a debug message to indicate this
            LOGGER.debugf("Could not find a container named %s", testDeployment.getTargetDescription().getName());
            return;
        }
        final Map<String, String> config = container.getContainerConfiguration().getContainerProperties();
        WildFlyArquillianConfiguration.addConfiguration(config, protocolArchive);
    }

    private Container<?> findContainer(final String targetName) {
        final var registry = containerRegistry.get();
        if (registry == null) {
            return null;
        }
        final var containers = registry.getContainers();
        if (containers.size() == 1) {
            return containers.get(0);
        }
        if ("_DEFAULT_".equals(targetName)) {
            // Find the default container
            for (Container<?> container : containers) {
                if (container.getContainerConfiguration().isDefault()) {
                    return container;
                }
            }
        }
        return registry.getContainer(targetName);
    }
}

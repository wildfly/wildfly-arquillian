/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.protocol.extension;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.test.impl.domain.ProtocolDefinition;
import org.jboss.arquillian.container.test.impl.domain.ProtocolRegistry;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.container.WebContainer;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class DetermineProtocolObserver {

    @Inject
    private Instance<ProtocolRegistry> protocolRegistry;

    public void determineProtocol(@Observes BeforeDeploy event) {
        final DeploymentDescription deployment = event.getDeployment();
        final ProtocolRegistry registry = protocolRegistry.get();
        final ProtocolDefinition protocol = registry.getProtocol(deployment.getProtocol());
        // Add a file with the protocol used
        addProtocol(deployment.getTestableArchive(), protocol);
    }

    private static void addProtocol(final Archive<?> archive, final ProtocolDefinition protocol) {
        final Asset asset = new StringAsset(protocol == null ? "\n" : protocol.getName() + "\n");
        if (archive instanceof WebContainer) {
            ((WebContainer<?>) archive).addAsWebInfResource(asset, "/classes/protocol.txt");
        } else {
            archive.add(asset, "protocol.txt");
        }
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.embedded;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.as.arquillian.container.CommonContainerExtension;

/**
 * Registers extension points defining the Embedded Container
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class EmbeddedContainerExtension extends CommonContainerExtension {

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.as.arquillian.container.CommonContainerExtension#register(org.jboss.arquillian.core.spi.LoadableExtension.ExtensionBuilder)
     */
    @Override
    public void register(final ExtensionBuilder builder) {
        super.register(builder);
        builder.service(DeployableContainer.class, EmbeddedDeployableContainer.class);
    }
}

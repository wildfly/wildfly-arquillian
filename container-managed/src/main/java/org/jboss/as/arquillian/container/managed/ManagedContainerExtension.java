/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.as.arquillian.container.CommonContainerExtension;

/**
 * The extensions used by the managed container.
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 * @author Thomas.Diesler@jboss.com
 * @since 02-Jun-2011
 */
public class ManagedContainerExtension extends CommonContainerExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        super.register(builder);
        builder.service(DeployableContainer.class, ManagedDeployableContainer.class);
        builder.service(ResourceProvider.class, AppClientProvider.class);
    }
}

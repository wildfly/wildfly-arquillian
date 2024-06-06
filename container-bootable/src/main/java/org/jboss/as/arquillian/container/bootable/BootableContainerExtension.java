/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.bootable;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.as.arquillian.container.CommonContainerExtension;

/**
 * The extensions used by the managed container.
 *
 * @author jdenise@redhat.com
 */
public class BootableContainerExtension extends CommonContainerExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        super.register(builder);
        builder.service(DeployableContainer.class, BootableDeployableContainer.class);
    }
}

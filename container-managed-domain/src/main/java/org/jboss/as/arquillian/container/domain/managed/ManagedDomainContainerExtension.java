/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.domain.managed;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.as.arquillian.container.domain.CommonContainerExtension;

/**
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class ManagedDomainContainerExtension extends CommonContainerExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        super.register(builder);
        builder.service(DeployableContainer.class, ManagedDomainDeployableContainer.class);
    }
}

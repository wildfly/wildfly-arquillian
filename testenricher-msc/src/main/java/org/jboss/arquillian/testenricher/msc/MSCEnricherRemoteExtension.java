/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.arquillian.testenricher.msc;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * MSCEnricherExtension
 *
 * @author Thomas.Diesler@jboss.com
 * @since 07-Jun-2011
 */
public class MSCEnricherRemoteExtension implements RemoteLoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        // Don't load the MSCTestEnricher unless the MSC classes can be found at runtime
        if (Validate.classExists("org.jboss.msc.service.ServiceContainer")) {
            builder.service(ResourceProvider.class, ServiceContainerProvider.class);
            builder.service(ResourceProvider.class, ServiceTargetProvider.class);
        }
    }

}

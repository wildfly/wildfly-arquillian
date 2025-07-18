/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * An extension register resources for in-container testing.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ManagedContainerRemoteExtension implements RemoteLoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        builder
                .observer(InContainerManagementClientProvider.class)
                .service(ResourceProvider.class, InContainerManagementClientProvider.class);
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.domain;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.wildfly.arquillian.domain.container.controller.DomainContainerControllerProvider;
import org.wildfly.arquillian.domain.container.controller.InContainerDomainContainerControllerCreator;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class DomainRemoteExtension implements RemoteLoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(ResourceProvider.class, DomainContainerControllerProvider.class)
                .observer(InContainerDomainContainerControllerCreator.class);
    }
}

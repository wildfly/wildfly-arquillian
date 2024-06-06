/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.controller;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * WildFlyContainerControllerRemoteExtension
 *
 * @author Radoslav Husar
 * @version Jan 2015
 */
public class WildFlyContainerControllerRemoteExtension implements RemoteLoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder
                .service(ResourceProvider.class, InContainerWildFlyContainerControllerProvider.class)
                .observer(InContainerWildFlyContainerControllerCreator.class);
    }
}

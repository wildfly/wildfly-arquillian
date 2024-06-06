/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.controller;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * WildFlyContainerControllerArquillianDeploymentAppender
 *
 * @author Radoslav Husar
 * @version Jan 2015
 */
public class WildFlyContainerControllerArquillianDeploymentAppender extends CachedAuxilliaryArchiveAppender {

    @Override
    protected Archive<?> buildArchive() {
        return ShrinkWrap
                .create(JavaArchive.class, "arquillian-wildfly-container-controller.jar")
                .addPackages(
                        true,
                        "org.jboss.as.arquillian.api",
                        "org.jboss.as.arquillian.container.controller")
                .addAsServiceProvider(RemoteLoadableExtension.class, WildFlyContainerControllerRemoteExtension.class);
    }
}
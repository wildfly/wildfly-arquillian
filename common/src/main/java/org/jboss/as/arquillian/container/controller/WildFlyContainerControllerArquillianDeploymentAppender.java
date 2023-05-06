/*
 * Copyright 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
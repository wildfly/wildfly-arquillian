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
                .observer(InContainerWildFlyContainerControllerCreator.class)
        ;
    }
}

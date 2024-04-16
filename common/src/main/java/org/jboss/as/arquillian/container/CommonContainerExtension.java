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
package org.jboss.as.arquillian.container;

import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestEnricher;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.as.arquillian.container.controller.ClientWildFlyContainerControllerCreator;
import org.jboss.as.arquillian.container.controller.ClientWildFlyContainerControllerProvider;
import org.jboss.as.arquillian.container.controller.WildFlyContainerLifecycleController;
import org.jboss.as.arquillian.container.controller.command.WildFlyContainerCommandObserver;

/**
 * The extensions used by the any jboss container.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 02-Jun-2011
 */
public class CommonContainerExtension implements LoadableExtension {

    @Override
    public void register(final ExtensionBuilder builder) {

        builder.service(DeploymentExceptionTransformer.class, ExceptionTransformer.class);
        builder.service(ResourceProvider.class, ArchiveDeployerProvider.class);
        builder.service(ResourceProvider.class, ManagementClientProvider.class);
        builder.service(TestEnricher.class, ContainerResourceTestEnricher.class);
        builder.service(AuxiliaryArchiveAppender.class, CommonContainerArchiveAppender.class);

        builder.observer(ServerSetupObserver.class);

        // WildFlyContainerController
        builder
                .service(ResourceProvider.class, ClientWildFlyContainerControllerProvider.class)
                .observer(ClientWildFlyContainerControllerCreator.class)
                .observer(WildFlyContainerCommandObserver.class)
                .observer(WildFlyContainerLifecycleController.class);

    }
}

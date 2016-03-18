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
package org.jboss.as.arquillian.container.domain;

import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.wildfly.arquillian.domain.DomainArquillianDeploymentAppender;
import org.wildfly.arquillian.domain.ServerGroupDeploymentObserver;
import org.wildfly.arquillian.domain.container.controller.ClientDomainContainerControllerCreator;
import org.wildfly.arquillian.domain.container.controller.command.DomainContainerCommandObserver;
import org.wildfly.arquillian.domain.container.controller.DomainContainerControllerProvider;

/**
 * The extensions used by the any jboss container.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 02-Jun-2011
 */
public class CommonContainerExtension implements LoadableExtension {

    @Override
    public void register(final ExtensionBuilder builder) {
        builder
                // Register services
                .service(DeploymentExceptionTransformer.class, ExceptionTransformer.class)
                .service(ResourceProvider.class, ManagementClientProvider.class)
                .service(ResourceProvider.class, DomainContainerControllerProvider.class)
                .service(AuxiliaryArchiveAppender.class, DomainArquillianDeploymentAppender.class)
                // Register observers
                .observer(ClientDomainContainerControllerCreator.class)
                .observer(DomainContainerCommandObserver.class)
                .observer(ServerGroupDeploymentObserver.class);
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.domain;

import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.wildfly.arquillian.domain.DomainArquillianDeploymentAppender;
import org.wildfly.arquillian.domain.ServerGroupDeploymentObserver;
import org.wildfly.arquillian.domain.container.controller.ClientDomainContainerControllerCreator;
import org.wildfly.arquillian.domain.container.controller.DomainContainerControllerProvider;
import org.wildfly.arquillian.domain.container.controller.command.DomainContainerCommandObserver;

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
                .service(ResourceProvider.class, ArchiveDeployerProvider.class)
                .service(ResourceProvider.class, ManagementClientProvider.class)
                .service(ResourceProvider.class, DomainContainerControllerProvider.class)
                .service(AuxiliaryArchiveAppender.class, DomainArquillianDeploymentAppender.class)
                // Register observers
                .observer(ClientDomainContainerControllerCreator.class)
                .observer(DomainContainerCommandObserver.class)
                .observer(ServerGroupDeploymentObserver.class);
    }
}

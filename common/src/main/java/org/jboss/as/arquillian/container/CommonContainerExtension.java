/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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
        // Set up the providers for client injection of a ServerManager. We will not support injection for in-container
        // tests. The main reason for this is we likely shouldn't be managing a servers lifecycle from a deployment. In
        // some cases it may not even work.
        builder.service(ResourceProvider.class, ServerManagerProvider.class);
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

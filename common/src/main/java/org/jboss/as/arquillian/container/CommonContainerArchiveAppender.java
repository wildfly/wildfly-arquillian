/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.setup.ConfigureLoggingSetupTask;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.plugin.tools.OperationExecutionException;

/**
 * Creates a library to add to deployments for common container based dependencies for in-container tests.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class CommonContainerArchiveAppender implements AuxiliaryArchiveAppender {

    @Override
    public Archive<?> createAuxiliaryArchive() {
        return ShrinkWrap.create(JavaArchive.class, "wildfly-common-testencricher.jar")
                // These two types are added to avoid exceptions with class loading for in-container tests. These
                // shouldn't really be used for in-container tests.
                .addClasses(ServerSetupTask.class, ServerSetup.class)
                .addClasses(
                        ManagementClient.class,
                        Authentication.class,
                        ManagedContainerRemoteExtension.class,
                        WildFlyArquillianConfiguration.class,
                        InContainerManagementClientProvider.class)
                // Add the setup task implementations
                .addPackage(ConfigureLoggingSetupTask.class.getPackage())
                // Adds wildfly-plugin-tools, this exception itself is explicitly needed
                .addPackages(true, OperationExecutionException.class.getPackage())
                .addAsServiceProvider(RemoteLoadableExtension.class, ManagedContainerRemoteExtension.class)
                .setManifest(new StringAsset("Manifest-Version: 1.0\r\n"
                        + "Dependencies: org.jboss.as.controller-client export,org.jboss.dmr export\r\n"));
    }
}

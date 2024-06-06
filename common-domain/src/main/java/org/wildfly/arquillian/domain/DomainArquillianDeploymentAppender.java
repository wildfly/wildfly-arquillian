/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.domain;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.CachedAuxilliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class DomainArquillianDeploymentAppender extends CachedAuxilliaryArchiveAppender {

    @Override
    protected Archive<?> buildArchive() {
        return ShrinkWrap.create(JavaArchive.class, "wildfly-arquillian-domain.jar")
                .addPackages(
                        true,
                        "org.wildfly.arquillian.domain.api",
                        "org.wildfly.arquillian.domain.container.controller")
                .addClasses(AbstractTargetsContainerProvider.class, DomainRemoteExtension.class)
                .addAsServiceProvider(RemoteLoadableExtension.class, DomainRemoteExtension.class);
    }
}
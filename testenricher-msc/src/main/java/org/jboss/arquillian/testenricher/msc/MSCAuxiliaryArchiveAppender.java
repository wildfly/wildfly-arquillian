/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.arquillian.testenricher.msc;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * MSCAuxiliaryArchiveAppender
 *
 * @author Thomas.Diesler@jboss.com
 * @since 07-Jun-2011
 */
public class MSCAuxiliaryArchiveAppender implements AuxiliaryArchiveAppender {

    @Override
    public Archive<?> createAuxiliaryArchive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "arquillian-testenricher-msc.jar");
        archive.addAsServiceProvider(RemoteLoadableExtension.class, MSCEnricherRemoteExtension.class);
        archive.addPackage(MSCEnricherExtension.class.getPackage());
        return archive;
    }
}

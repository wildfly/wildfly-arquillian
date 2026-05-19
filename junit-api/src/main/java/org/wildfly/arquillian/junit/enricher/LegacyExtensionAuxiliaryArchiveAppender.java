/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.junit.enricher;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.arquillian.junit.annotations.JBossHome;
import org.wildfly.arquillian.junit.condition.JBossHomeParameterResolver;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class LegacyExtensionAuxiliaryArchiveAppender implements AuxiliaryArchiveAppender {
    @Override
    public Archive<?> createAuxiliaryArchive() {
        return ShrinkWrap.create(JavaArchive.class, "wildfly-junit-legacy-utils.jar")
                .addPackage(JBossHome.class.getPackage())
                .addPackage(JBossHomeParameterResolver.class.getPackage());
    }
}

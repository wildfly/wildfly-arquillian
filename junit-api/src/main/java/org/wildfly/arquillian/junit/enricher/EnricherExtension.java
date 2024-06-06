/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.junit.enricher;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class EnricherExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        builder.service(AuxiliaryArchiveAppender.class, ExtensionAuxiliaryArchiveAppender.class);
    }
}

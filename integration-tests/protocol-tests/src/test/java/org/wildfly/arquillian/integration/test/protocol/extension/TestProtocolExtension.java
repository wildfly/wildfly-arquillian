/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.protocol.extension;

import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class TestProtocolExtension implements LoadableExtension {
    @Override
    public void register(final ExtensionBuilder builder) {
        builder.observer(DetermineProtocolObserver.class);
    }
}

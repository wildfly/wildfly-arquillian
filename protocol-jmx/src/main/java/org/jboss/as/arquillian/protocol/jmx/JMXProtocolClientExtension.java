/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.protocol.jmx;

import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * JMXProtocolClientExtension
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2011
 */
public class JMXProtocolClientExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(Protocol.class, ExtendedJMXProtocol.class);
        builder.observer(ArquillianServiceDeployer.class);
        builder.observer(ServerKillerExtension.class);
    }
}

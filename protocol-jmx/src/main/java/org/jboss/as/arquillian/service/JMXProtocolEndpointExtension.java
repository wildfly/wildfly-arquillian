/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.service;

import org.jboss.arquillian.protocol.jmx.JMXExtension;

/**
 * JMXProtocolEndpointExtension
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2011
 */
public class JMXProtocolEndpointExtension extends JMXExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        super.register(builder);
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.protocol.jmx;

import org.jboss.arquillian.protocol.jmx.JMXProtocolConfiguration;

/**
 * The JBossAS JMXProtocol extension.
 *
 * @author thomas.diesler@jboss.com
 * @since 27-Feb-2015
 */
public class ExtendedJMXProtocolConfiguration extends JMXProtocolConfiguration {

    public static final String PROPERTY_ENABLE_TCCL = "enableThreadContextClassLoader";

    private boolean enableThreadContextClassLoader = true;

    public boolean isEnableThreadContextClassLoader() {
        return enableThreadContextClassLoader;
    }

    public void setEnableThreadContextClassLoader(boolean enableThreadContextClassLoader) {
        this.enableThreadContextClassLoader = enableThreadContextClassLoader;
    }
}

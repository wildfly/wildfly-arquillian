/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.jboss.logging.Logger;

/**
 * A provider for the JSR160 connection.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 03-Dec-2010
 */
public final class MBeanServerConnectionProvider implements Closeable {

    private static final Logger log = Logger.getLogger(MBeanServerConnectionProvider.class);
    private final InetAddress hostAddr;
    private final int port;

    private JMXConnector jmxConnector;

    public static MBeanServerConnectionProvider defaultProvider() throws UnknownHostException {
        return new MBeanServerConnectionProvider(InetAddress.getByName("127.0.0.1"), 9990);
    }

    public MBeanServerConnectionProvider(InetAddress hostAddr, int port) {
        this.hostAddr = hostAddr;
        this.port = port;
    }

    public MBeanServerConnection getConnection() {
        String host = hostAddr.getHostAddress();
        String urlString = System.getProperty("jmx.service.url",
                "service:jmx:remote+http://" + NetworkUtils.formatPossibleIpv6Address(host) + ":" + port);
        try {
            if (jmxConnector == null) {
                log.debug("Connecting JMXConnector to: " + urlString);
                JMXServiceURL serviceURL = new JMXServiceURL(urlString);
                jmxConnector = JMXConnectorFactory.connect(serviceURL, null);
            }
            return jmxConnector.getMBeanServerConnection();
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot obtain MBeanServerConnection to: " + urlString, ex);
        }
    }

    public MBeanServerConnection getConnection(long timeout) {
        while (timeout > 0) {
            try {
                return getConnection();
            } catch (Exception ex) {
                // ignore
            }
            try {
                Thread.sleep(100);
                timeout -= 100;
            } catch (InterruptedException ex) {
                // ignore
            }
        }
        throw new IllegalStateException("MBeanServerConnection not available");
    }

    @Override
    public void close() throws IOException {
        if (jmxConnector != null)
            try {
                jmxConnector.close();
            } catch (Throwable ignore) {
            }
    }
}

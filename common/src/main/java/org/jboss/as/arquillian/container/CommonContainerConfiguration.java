/*
 * Copyright 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.arquillian.container;

import java.net.URI;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

/**
 * JBossAS7 server configuration
 *
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class CommonContainerConfiguration implements ContainerConfiguration {

    private String managementProtocol = "remote+http";
    private String managementAddress;
    private int managementPort;

    private String username;
    private String password;
    private String authenticationConfig;
    private String host;
    private int port;

    /**
     * Optional connection timeout in millis.
     */
    private int connectionTimeout;

    public CommonContainerConfiguration() {
        managementAddress = "127.0.0.1";
        managementPort = 9990 + Integer.decode(System.getProperty("jboss.socket.binding.port-offset", "0"));
        host = null;
        port = -1;
    }

    public String getManagementAddress() {
        return managementAddress;
    }

    public void setManagementAddress(String host) {
        this.managementAddress = host;
    }

    public int getManagementPort() {
        return managementPort;
    }

    public void setManagementPort(int managementPort) {
        this.managementPort = managementPort;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getManagementProtocol() {
        return managementProtocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setManagementProtocol(final String managementProtocol) {
        this.managementProtocol = managementProtocol;
    }

    public int getConnectionTimeout() {
        return this.connectionTimeout;
    }

    /**
     * Accepts a timeout which must be greater than 0.
     */
    public void setConnectionTimeout(final int connectionTimeout) {
        if (connectionTimeout <= 0) {
            throw new IllegalArgumentException("Connection timeout " + connectionTimeout + " must be > 0");
        }
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * The {@linkplain URI URI} path for the authentication configuration.
     *
     * @return the URI for the path or {@code null} if no path was set
     */
    public String getAuthenticationConfig() {
        return authenticationConfig;
    }

    /**
     * Set the {@linkplain URI URI} path for the authentication configuration.
     *
     * @param authenticationConfig the URI path
     */
    public void setAuthenticationConfig(final String authenticationConfig) {
        this.authenticationConfig = authenticationConfig;
    }

    @Override
    public void validate() throws ConfigurationException {
        if (username != null && password == null) {
            throw new ConfigurationException("username has been set, but no password given");
        }
    }
}

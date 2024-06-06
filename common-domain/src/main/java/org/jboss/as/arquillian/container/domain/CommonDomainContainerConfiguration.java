/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.domain;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

/**
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class CommonDomainContainerConfiguration implements ContainerConfiguration {

    private String managementAddress;
    private int managementPort;

    private String username;
    private String password;
    private String authenticationConfig;

    private String protocol = "http";
    private String socketBindingName;

    private Map<String, String> containerNameMap;

    private Map<String, String> containerModeMap;

    private int serverGroupOperationTimeoutInSeconds = 120;

    private int serverOperationTimeoutInSeconds = 120;

    public CommonDomainContainerConfiguration() {
        managementAddress = "127.0.0.1";
        managementPort = 9990 + Integer.decode(System.getProperty("jboss.socket.binding.port-offset", "0"));
    }

    public InetAddress getManagementAddress() {
        return getInetAddress(managementAddress);
    }

    String getManagementHostName() {
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

    private InetAddress getInetAddress(String name) {
        try {
            return InetAddress.getByName(name);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unknown host: " + name);
        }
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

    /**
     * @return the containerNameMap
     */
    public Map<String, String> getContainerNameMap() {
        if (containerNameMap == null) {
            return new HashMap<String, String>();
        }
        return containerNameMap;
    }

    /**
     * Change the container name as seen by Arquillian of the Servers or ServerGroups in the Domain.
     * format: host:server-name=new-name,server-group-name=crm-servers
     *
     * @param containerNameMap
     */
    public void setContainerNameMap(String containerNameMap) {
        this.containerNameMap = convertToMap(containerNameMap);
    }

    /**
     * @return the containerModeMap
     */
    public Map<String, String> getContainerModeMap() {
        if (containerModeMap == null) {
            return new HashMap<String, String>();
        }
        return containerModeMap;
    }

    /**
     * Change the container mode of the Servers or ServerGroups in the Domain.
     * format: host:server-name=manual,host:.*=suite
     *
     * @param containerModeString
     */
    public void setContainerModeMap(String containerModeString) {
        this.containerModeMap = convertToMap(containerModeString);
    }

    /**
     * The number of seconds to wait before failing when starting/stopping a server group in the Domain.
     *
     * @param serverGroupStartupTimeoutInSeconds
     */
    public void setServerGroupOperationTimeoutInSeconds(int serverGroupStartupTimeoutInSeconds) {
        this.serverGroupOperationTimeoutInSeconds = serverGroupStartupTimeoutInSeconds;
    }

    public int getServerGroupOperationTimeoutInSeconds() {
        return serverGroupOperationTimeoutInSeconds;
    }

    /**
     * The number of seconds to wait before failing when starting/stopping a single server in the Domain.
     *
     * @param serverStartupTimeoutInSeconds
     */
    public void setServerOperationTimeoutInSeconds(int serverStartupTimeoutInSeconds) {
        this.serverOperationTimeoutInSeconds = serverStartupTimeoutInSeconds;
    }

    public int getServerOperationTimeoutInSeconds() {
        return serverOperationTimeoutInSeconds;
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

    /**
     * Returns the protocol to for HTTP connections. This currently only supports http and https with a default of http.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the protocol for HTTP connections. If {@code null} the default of http is used.
     *
     * @param protocol the protocol to use, this must be http or https
     */
    public void setProtocol(final String protocol) {
        this.protocol = protocol == null ? "http" : protocol;
        if (!("http".equalsIgnoreCase(this.protocol) || "https".equalsIgnoreCase(this.protocol))) {
            throw new ConfigurationException("Only http and https are allowed protocol settings, found " + protocol);
        }
    }

    /**
     * Returns the socket binding name to use.
     *
     * @return the socket binding name or {@code null} to discover one
     */
    public String getSocketBindingName() {
        return socketBindingName;
    }

    /**
     * Sets the socket binding name to use for determining the host and port for HTTP connections. This can be used to
     * override discovering the first binding name.
     * <p>
     * The socket binding name is configured in WildFly. If this is not set, one will be determined from the Undertow
     * subsystem.
     * </p>
     *
     * @param socketBindingName the socket binding name or {@code null} for one to be determined
     */
    public void setSocketBindingName(final String socketBindingName) {
        this.socketBindingName = socketBindingName;
    }

    @Override
    public void validate() throws ConfigurationException {
        if (username != null && password == null) {
            throw new ConfigurationException("username has been set, but no password given");
        }
        if (protocol != null && !("http".equalsIgnoreCase(protocol) || "https".equalsIgnoreCase(protocol))) {
            throw new ConfigurationException("Only http and https are allowed protocol settings, found " + protocol);
        }
    }

    private Map<String, String> convertToMap(String data) {
        Map<String, String> map = new HashMap<String, String>();
        String[] values = data.split(",");

        for (String value : values) {
            String[] content = value.split("=");
            if (content.length != 2) {
                throw new IllegalArgumentException(
                        "Could not parse map data from '" + data + "'. Missing value or key in '" + value + "'");
            }
            map.put(clean(content[0]), clean(content[1]));
        }
        return map;
    }

    private String clean(String data) {
        return data.replaceAll("\\r\\n|\\r|\\n", " ").trim();
    }
}
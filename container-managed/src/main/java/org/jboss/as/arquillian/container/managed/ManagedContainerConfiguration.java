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
package org.jboss.as.arquillian.container.managed;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.as.arquillian.container.DistributionContainerConfiguration;

/**
 * The managed container configuration
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author Thomas.Diesler@jboss.com
 */
public class ManagedContainerConfiguration extends DistributionContainerConfiguration {

    /**
     * Default timeout value waiting on ports is 10 seconds
     */
    private static final Integer DEFAULT_VALUE_WAIT_FOR_PORTS_TIMEOUT_SECONDS = 10;

    private String javaVmArguments = System.getProperty("jboss.options");

    private String jbossArguments;

    private int startupTimeoutInSeconds = 60;

    private int stopTimeoutInSeconds = 60;

    private boolean outputToConsole = true;

    private String serverConfig = System.getProperty("jboss.server.config.file.name");

    private boolean allowConnectingToRunningServer = Boolean.parseBoolean(System.getProperty("allowConnectingToRunningServer", "false"));

    private boolean enableAssertions = true;

    private boolean adminOnly = false;

    private Integer[] waitForPorts;

    private Integer waitForPortsTimeoutInSeconds;

    private boolean setupCleanServerBaseDir = false;

    private String cleanServerBaseDir;

    private boolean debugMode = false;

    private int debugPort = 8787;

    private boolean debugSuspend = false;

    public ManagedContainerConfiguration() {
        if (System.getProperty("jboss.debug", "false").equalsIgnoreCase("true")) {
            debugMode = true;
        }
        if (System.getProperty("jboss.debug.port") != null) {
            try {
                int port = Integer.parseInt(System.getProperty("jboss.debug.port"));
                // linux does not allow allocation of ports lower than 1024 if non root
                if (port > 1024) {
                    debugPort = port;
                }
            } catch (NumberFormatException e) {
                // do nothing
            }
        }
        if (System.getProperty("jboss.debug.suspend", "false").equalsIgnoreCase("true")) {
            debugSuspend = true;
        }
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();
    }

    public String getJavaVmArguments() {
        return javaVmArguments;
    }

    public void setJavaVmArguments(String javaVmArguments) {
        this.javaVmArguments = javaVmArguments;
    }

    public String getJbossArguments() {
        return jbossArguments;
    }

    public void setJbossArguments(String jbossArguments) {
        this.jbossArguments = jbossArguments;
    }

    public void setStartupTimeoutInSeconds(int startupTimeoutInSeconds) {
        this.startupTimeoutInSeconds = startupTimeoutInSeconds;
    }

    public int getStartupTimeoutInSeconds() {
        return startupTimeoutInSeconds;
    }

    public void setStopTimeoutInSeconds(int stopTimeoutInSeconds) {
        this.stopTimeoutInSeconds = stopTimeoutInSeconds;
    }

    /**
     * Number of seconds to wait for the container process to shutdown; defaults to 60
     */
    public int getStopTimeoutInSeconds() {
        return stopTimeoutInSeconds;
    }

    public void setOutputToConsole(boolean outputToConsole) {
        this.outputToConsole = outputToConsole;
    }

    public boolean isOutputToConsole() {
        return outputToConsole;
    }

    /**
     * Get the server configuration file name. Equivalent to [-server-config=...] on the command line.
     */
    public String getServerConfig() {
        return serverConfig;
    }

    /**
     * Set the server configuration file name. Equivalent to [-server-config=...] on the command line.
     */
    public void setServerConfig(String serverConfig) {
        this.serverConfig = serverConfig;
    }

    public boolean isAllowConnectingToRunningServer() {
        return allowConnectingToRunningServer;
    }

    public void setAllowConnectingToRunningServer(final boolean allowConnectingToRunningServer) {
        this.allowConnectingToRunningServer = allowConnectingToRunningServer;
    }

    public boolean isEnableAssertions() {
        return enableAssertions;
    }

    public void setEnableAssertions(final boolean enableAssertions) {
        this.enableAssertions = enableAssertions;
    }

    public Integer[] getWaitForPorts() {
        return waitForPorts;
    }

    public void setWaitForPorts(String waitForPorts) {
        final Scanner scanner = new Scanner(waitForPorts);
        final List<Integer> list = new ArrayList<Integer>();
        while (scanner.hasNextInt()) {
            list.add(scanner.nextInt());
        }
        this.waitForPorts = list.toArray(new Integer[] {});
    }

    public Integer getWaitForPortsTimeoutInSeconds() {
        return waitForPortsTimeoutInSeconds != null ? waitForPortsTimeoutInSeconds
            : DEFAULT_VALUE_WAIT_FOR_PORTS_TIMEOUT_SECONDS;
    }

    public void setWaitForPortsTimeoutInSeconds(final Integer waitForPortsTimeoutInSeconds) {
        this.waitForPortsTimeoutInSeconds = waitForPortsTimeoutInSeconds;
    }

    public boolean isAdminOnly() {
        return adminOnly;
    }

    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    public boolean isSetupCleanServerBaseDir() {
        return setupCleanServerBaseDir;
    }

    public void setSetupCleanServerBaseDir(boolean setupCleanServerBaseDir) {
        this.setupCleanServerBaseDir = setupCleanServerBaseDir;
    }

    public String getCleanServerBaseDir() {
        return cleanServerBaseDir;
    }

    public void setCleanServerBaseDir(String cleanServerBaseDir) {
        this.cleanServerBaseDir = cleanServerBaseDir;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public int getDebugPort() {
        return debugPort;
    }

    public void setDebugPort(int debugPort) {
        this.debugPort = debugPort;
    }

    public boolean isDebugSuspend() {
        return debugSuspend;
    }

    public void setDebugSuspend(boolean debugSuspend) {
        this.debugSuspend = debugSuspend;
    }
}

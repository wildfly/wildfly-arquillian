/*
 * Copyright 2020 Red Hat, Inc.
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
package org.jboss.as.arquillian.container.bootable;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.as.arquillian.container.CommonContainerConfiguration;

/**
 * The managed container configuration
 *
 * @author jdenise@redhat.com
 */
public class BootableContainerConfiguration extends CommonContainerConfiguration {

    private String javaHome = System.getenv("JAVA_HOME");

    private String jarFile;
    private String installDir;

    /**
     * Default timeout value waiting on ports is 10 seconds
     */
    private static final Integer DEFAULT_VALUE_WAIT_FOR_PORTS_TIMEOUT_SECONDS = 10;

    private String javaVmArguments = System.getProperty("jboss.options");

    private String jbossArguments;

    private int startupTimeoutInSeconds = 60;

    private int stopTimeoutInSeconds = 60;

    private boolean outputToConsole = true;

    private boolean allowConnectingToRunningServer = Boolean.parseBoolean(System.getProperty("allowConnectingToRunningServer", "false"));

    private boolean enableAssertions = true;

    private Integer[] waitForPorts;

    private Integer waitForPortsTimeoutInSeconds;

    private boolean setupCleanServerBaseDir = false;

    private String cleanServerBaseDir;

    public BootableContainerConfiguration() {
        // if no javaHome is set use java.home of already running jvm
        if (javaHome == null || javaHome.isEmpty()) {
            javaHome = System.getProperty("java.home");
        }
    }

    @Override
    public void validate() throws ConfigurationException {
        super.validate();
    }

    /**
     * @return the javaHome
     */
    public String getJavaHome() {
        return javaHome;
    }

    /**
     * @param javaHome the javaHome to set
     */
    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
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
     * Number of seconds to wait for the container process to shutdown; defaults
     * to 60
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
     * Get the bootable jar file.
     */
    public String getJarFile() {
        return jarFile;
    }

    /**
     * Set the bootable jar file.
     */
    public void setJarFile(String jarFile) {
        this.jarFile = jarFile;
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
        this.waitForPorts = list.toArray(new Integer[]{});
    }

    public Integer getWaitForPortsTimeoutInSeconds() {
        return waitForPortsTimeoutInSeconds != null ? waitForPortsTimeoutInSeconds
                : DEFAULT_VALUE_WAIT_FOR_PORTS_TIMEOUT_SECONDS;
    }

    public void setWaitForPortsTimeoutInSeconds(final Integer waitForPortsTimeoutInSeconds) {
        this.waitForPortsTimeoutInSeconds = waitForPortsTimeoutInSeconds;
    }

    public String getInstallDir() {
        return installDir;
    }

    public void setInstallDir(String installDir) {
        this.installDir = installDir;
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
}

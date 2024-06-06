/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.bootable;

import org.jboss.as.arquillian.container.CommonManagedContainerConfiguration;

/**
 * The managed container configuration
 *
 * @author jdenise@redhat.com
 * @since 3.0.0
 */
@SuppressWarnings({ "InstanceVariableMayNotBeInitialized", "unused" })
public class BootableContainerConfiguration extends CommonManagedContainerConfiguration {

    private String jarFile;
    private String installDir;

    private String javaVmArguments = System.getProperty("jboss.options");

    private boolean debug = getBooleanProperty("wildfly.debug", false);
    private int debugPort = Integer.parseInt(System.getProperty("wildfly.debug.port", "8787"));

    private boolean debugSuspend = getBooleanProperty("wildfly.debug.suspend", true);

    private String jbossArguments;

    private boolean enableAssertions = true;

    public String getJavaVmArguments() {
        return javaVmArguments;
    }

    public void setJavaVmArguments(String javaVmArguments) {
        this.javaVmArguments = javaVmArguments;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    public int getDebugPort() {
        return debugPort;
    }

    public void setDebugPort(final int debugPort) {
        this.debugPort = debugPort;
    }

    public boolean isDebugSuspend() {
        return debugSuspend;
    }

    public void setDebugSuspend(final boolean debugSuspend) {
        this.debugSuspend = debugSuspend;
    }

    public String getJbossArguments() {
        return jbossArguments;
    }

    public void setJbossArguments(String jbossArguments) {
        this.jbossArguments = jbossArguments;
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

    public boolean isEnableAssertions() {
        return enableAssertions;
    }

    public void setEnableAssertions(final boolean enableAssertions) {
        this.enableAssertions = enableAssertions;
    }

    public String getInstallDir() {
        return installDir;
    }

    public void setInstallDir(String installDir) {
        this.installDir = installDir;
    }

    private static boolean getBooleanProperty(final String key, final boolean dft) {
        final String value = System.getProperty(key);
        if (value != null) {
            return value.isBlank() || Boolean.parseBoolean(value);
        }
        return dft;
    }
}

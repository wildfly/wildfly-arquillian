/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.domain.api;

/**
 * A manager that allows control of the server groups lifecycle. Note that if this is not associated with a manual mode
 * container operations will fail.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface ServerGroupManager {

    /**
     * Reloads all the servers associated with this server group.
     *
     * @param groupName the name of the server group
     *
     * @throws IllegalStateException if lifecycle operations are not allowed
     * @throws RuntimeException      if the reload operation fails
     */
    void reloadServers(String groupName);

    /**
     * Restarts all the servers associated with this server group.
     *
     * @param groupName the name of the server group
     *
     * @throws IllegalStateException if lifecycle operations are not allowed
     * @throws RuntimeException      if the restart operation fails
     */
    void restartServers(String groupName);

    /**
     * Resumes all the previously suspended servers associated with this server group.
     *
     * @param groupName the name of the server group
     *
     * @throws IllegalStateException if lifecycle operations are not allowed
     * @throws RuntimeException      if the resume operation fails
     */
    void resumeServers(String groupName);

    /**
     * Starts all the servers associated with this server group.
     *
     * @param groupName the name of the server group
     *
     * @throws IllegalStateException if lifecycle operations are not allowed
     * @throws RuntimeException      if the start operation fails
     */
    void startServers(String groupName);

    /**
     * Stops all the servers associated with this server group.
     *
     * @param groupName the name of the server group
     *
     * @throws IllegalStateException if lifecycle operations are not allowed
     * @throws RuntimeException      if the stop operation fails
     */
    void stopServers(String groupName);

    /**
     * Suspends all the servers associated with this server group.
     *
     * @param groupName the name of the server group
     * @param timeout   the timeout in seconds. A value of 0 returns immediately and a value of -1 will wait indefinitely
     *
     * @throws IllegalStateException if lifecycle operations are not allowed
     * @throws RuntimeException      if the suspend operation fails
     */
    void suspendServers(String groupName, int timeout);
}

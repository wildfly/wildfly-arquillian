/*
 * Copyright 2016 Red Hat, Inc.
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

package org.wildfly.arquillian.domain.api;

import org.jboss.arquillian.container.test.api.ContainerController;

/**
 * A container controller for domains which allows hosts and server groups to be queried. If the container is in manual
 * mode the hosts and server groups lifecycle can also be handled.
 * <p>
 * Note that to access any hosts or server groups the container must be started.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface DomainContainerController extends ContainerController {

    /**
     * Reloads all the servers associated with this server group.
     *
     * @param containerQualifier the name of the container
     * @param groupName          the name of the server group
     */
    void reloadServers(String containerQualifier, String groupName);

    /**
     * Restarts all the servers associated with this server group.
     *
     * @param containerQualifier the name of the container
     * @param groupName          the name of the server group
     */
    void restartServers(String containerQualifier, String groupName);

    /**
     * Resumes all the previously suspended servers associated with this server group.
     *
     * @param containerQualifier the name of the container
     * @param groupName          the name of the server group
     */
    void resumeServers(String containerQualifier, String groupName);

    /**
     * Starts all the servers associated with this server group.
     *
     * @param containerQualifier the name of the container
     * @param groupName          the name of the server group
     */
    void startServers(String containerQualifier, String groupName);

    /**
     * Stops all the servers associated with this server group.
     *
     * @param containerQualifier the name of the container
     * @param groupName          the name of the server group
     */
    void stopServers(String containerQualifier, String groupName);

    /**
     * Suspends all the servers associated with this server group.
     *
     * @param containerQualifier the name of the container
     * @param groupName          the name of the server group
     * @param timeout            the timeout in seconds. A value of 0 returns immediately and a value of -1 will wait indefinitely
     */
    void suspendServers(String containerQualifier, String groupName, int timeout);

    /**
     * Starts the server.
     *
     * @param containerQualifier the name of the container
     * @param hostName           the name of the host the server is associated with
     * @param serverName         the name of the server
     */
    void startServer(String containerQualifier, String hostName, String serverName);

    /**
     * Stops the server.
     *
     * @param containerQualifier the name of the container
     * @param hostName           the name of the host the server is associated with
     * @param serverName         the name of the server
     */
    void stopServer(String containerQualifier, String hostName, String serverName);

    /**
     * Checks the status of the server and returns {@code true} if the server is fully started.
     *
     * @param containerQualifier the name of the container
     * @param hostName           the name of the host the server is associated with
     * @param serverName         the name of the server
     *
     * @return {@code true} if the server is fully started, otherwise {@code false}
     */
    boolean isServerStarted(String containerQualifier, String hostName, String serverName);

    /**
     * Restarts the server.
     *
     * @param containerQualifier the name of the container
     * @param hostName           the name of the host the server is associated with
     * @param serverName         the name of the server
     */
    void restartServer(String containerQualifier, String hostName, String serverName);

    /**
     * Resumes this server after being suspended.
     *
     * @param containerQualifier the name of the container
     * @param hostName           the name of the host the server is associated with
     * @param serverName         the name of the server
     */
    void resumeServer(String containerQualifier, String hostName, String serverName);

    /**
     * Suspends this server.
     *
     * @param containerQualifier the name of the container
     * @param hostName           the name of the host the server is associated with
     * @param serverName         the name of the server
     * @param timeout            the timeout for the suspend
     */
    void suspendServer(String containerQualifier, String hostName, String serverName, int timeout);
}

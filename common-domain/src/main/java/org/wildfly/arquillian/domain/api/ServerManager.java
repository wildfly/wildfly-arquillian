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

/**
 * A manager that allows control of the server lifecycle. Note that if this is not associated with a manual mode
 * container operations will fail.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface ServerManager {

    /**
     * Starts the server.
     *
     * @param hostName   the name of the host the server is on
     * @param serverName the name of the server
     *
     * @throws IllegalStateException if lifecycle operations are not allowed
     * @throws RuntimeException      if the start operation fails
     */
    void startServer(String hostName, String serverName);

    /**
     * Stops the server.
     *
     * @param hostName   the name of the host the server is on
     * @param serverName the name of the server
     *
     * @throws IllegalStateException if lifecycle operations are not allowed
     * @throws RuntimeException      if the stop operation fails
     */
    void stopServer(String hostName, String serverName);

    /**
     * Checks the status of the server and returns {@code true} if the server is fully started.
     *
     * @param hostName   the name of the host the server is on
     * @param serverName the name of the server
     *
     * @return {@code true} if the server is fully started, otherwise {@code false}
     *
     * @throws RuntimeException if the operation fails
     */
    boolean isServerStarted(String hostName, String serverName);

    /**
     * Restarts the server.
     *
     * @param hostName   the name of the host the server is on
     * @param serverName the name of the server
     *
     * @throws IllegalStateException if lifecycle operations are not allowed
     * @throws RuntimeException      if the restart operation fails
     */
    void restartServer(String hostName, String serverName);

    /**
     * Resumes this server after being suspended.
     *
     * @param hostName   the name of the host the server is on
     * @param serverName the name of the server
     *
     * @throws IllegalStateException if lifecycle operations are not allowed
     * @throws RuntimeException      if the resume operation fails
     */
    void resumeServer(String hostName, String serverName);

    /**
     * Suspends this server.
     *
     * @param hostName   the name of the host the server is on
     * @param serverName the name of the server
     * @param timeout    the timeout for the suspend
     *
     * @throws IllegalStateException if lifecycle operations are not allowed
     * @throws RuntimeException      if the suspend operation fails
     */
    void suspendServer(String hostName, String serverName, int timeout);

    /**
     * The group name this server is associated with this server.
     *
     * @param hostName   the name of the host the server is on
     * @param serverName the name of the server
     *
     * @return the group name
     *
     * @throws RuntimeException if the operation fails
     */
    String getServerGroupName(String hostName, String serverName);
}

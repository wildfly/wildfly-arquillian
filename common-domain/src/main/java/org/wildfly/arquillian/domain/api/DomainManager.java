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

import java.util.Set;

/**
 * Allows a domain to be managed. If the container this manager belongs to was not started in manual mode any lifecycle
 * operations will result in an error.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface DomainManager extends ServerGroupManager, ServerManager {

    /**
     * Returns a set of the server group names.
     *
     * @return the server group names
     * @throws RuntimeException if the operation fails
     */
    Set<String> getServerGroups();

    /**
     * Returns all the servers in this domain.
     *
     * @return all the servers
     * @throws RuntimeException if the operation fails
     */
    Set<ServerDescription> getServers();

    /**
     * Returns all the servers on the host.
     *
     * @param hostName the host to get the serves for
     *
     * @return the servers associated with the host
     * @throws RuntimeException if the operation fails
     */
    Set<ServerDescription> getServers(String hostName);

    /**
     * If the domain container has been started.
     *
     * @return {@code true} if the domain controller has been started, otherwise {@code false}
     */
    boolean isDomainStarted();
}

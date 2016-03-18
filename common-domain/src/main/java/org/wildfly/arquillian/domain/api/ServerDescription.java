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
 * Describes the servers associations within the domain.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface ServerDescription extends Comparable<ServerDescription> {

    /**
     * The host name this server is associated with.
     *
     * @return the host name
     */
    String getHostName();

    /**
     * The name of this server.
     *
     * @return the name
     */
    String getName();

    /**
     * The group name this server is associated with.
     *
     * @return the group name
     */
    String getGroupName();

    @Override
    default int compareTo(final ServerDescription other) {
        int result = getHostName().compareTo(other.getHostName());
        if (result == 0) {
            result = getName().compareTo(other.getName());
        }
        return result;
    }
}

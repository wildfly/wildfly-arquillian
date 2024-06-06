/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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

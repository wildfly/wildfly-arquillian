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

package org.wildfly.arquillian.domain.container.controller.command;

import org.jboss.arquillian.container.test.impl.client.deployment.command.AbstractCommand;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ServerLifecycleCommand extends AbstractCommand<Boolean> {

    private final String containerQualifier;
    private final Lifecycle lifecycle;
    private final String hostName;
    private final String serverName;
    private final int timeout;

    public ServerLifecycleCommand(final String containerQualifier, final Lifecycle lifecycle, final String hostName,
            final String serverName) {
        this(containerQualifier, lifecycle, hostName, serverName, -1);
    }

    public ServerLifecycleCommand(final String containerQualifier, final Lifecycle lifecycle, final String hostName,
            final String serverName, final int timeout) {
        this.containerQualifier = containerQualifier;
        this.lifecycle = lifecycle;
        this.hostName = hostName;
        this.serverName = serverName;
        this.timeout = timeout;
    }

    public String getContainerQualifier() {
        return containerQualifier;
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public String getHostName() {
        return hostName;
    }

    public String getServerName() {
        return serverName;
    }

    public int getTimeout() {
        return timeout;
    }
}

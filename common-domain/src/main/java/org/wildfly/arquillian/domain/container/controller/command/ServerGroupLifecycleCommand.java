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
public class ServerGroupLifecycleCommand extends AbstractCommand<Boolean> {

    private final String containerQualifier;
    private final Lifecycle lifecycle;
    private final String serverGroupName;
    private final int timeout;

    public ServerGroupLifecycleCommand(final String containerQualifier, final Lifecycle lifecycle,
            final String serverGroupName) {
        this(containerQualifier, lifecycle, serverGroupName, -1);
    }

    public ServerGroupLifecycleCommand(final String containerQualifier, final Lifecycle lifecycle, final String serverGroupName,
            final int timeout) {
        this.containerQualifier = containerQualifier;
        this.lifecycle = lifecycle;
        this.serverGroupName = serverGroupName;
        this.timeout = timeout;
    }

    public String getContainerQualifier() {
        return containerQualifier;
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    public String getServerGroupName() {
        return serverGroupName;
    }

    public int getTimeout() {
        return timeout;
    }
}

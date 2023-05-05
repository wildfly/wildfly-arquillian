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

package org.jboss.as.arquillian.container.domain;

import org.jboss.as.controller.client.ModelControllerClient;
import org.wildfly.arquillian.domain.AbstractDomainManager;

/**
 * A domain manager to be used in a container.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class ContainerDomainManager extends AbstractDomainManager {

    private final ModelControllerClient client;
    private volatile boolean containerStarted;

    /**
     * Creates a new domain manager.
     *
     * @param containerName           the name of the container this domain manager belongs to
     * @param lifecycleControlAllowed {@code true} if the lifecycle operations are allowed
     * @param client                  the management client used to communicate with the running container
     */
    ContainerDomainManager(final String containerName, final boolean lifecycleControlAllowed,
            final ModelControllerClient client) {
        this(containerName, lifecycleControlAllowed, client, false);
    }

    /**
     * Creates a new domain manager.
     *
     * @param containerName           the name of the container this domain manager belongs to
     * @param lifecycleControlAllowed {@code true} if the lifecycle operations are allowed
     * @param client                  the management client used to communicate with the running container
     * @param containerStarted        the default container setting
     */
    ContainerDomainManager(final String containerName, final boolean lifecycleControlAllowed,
            final ModelControllerClient client, final boolean containerStarted) {
        super(containerName, lifecycleControlAllowed);
        this.client = client;
        this.containerStarted = containerStarted;
    }

    /**
     * Set to indicate whether or not the domain container has been started.
     *
     * @param containerStarted {@code true} if the domain container has been started, {@code false} if it has been stopped.
     */
    protected void setContainerStarted(final boolean containerStarted) {
        this.containerStarted = containerStarted;
    }

    @Override
    public boolean isDomainStarted() {
        return containerStarted;
    }

    @Override
    protected ModelControllerClient getModelControllerClient() {
        return client;
    }
}

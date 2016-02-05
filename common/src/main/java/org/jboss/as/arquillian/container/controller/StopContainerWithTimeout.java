/*
 * Copyright 2015 Red Hat, Inc.
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
package org.jboss.as.arquillian.container.controller;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.event.ContainerControlEvent;

/**
 * {@link ContainerControlEvent} implementation fired in {@link ClientWildFlyContainerController#stop(java.lang.String, int)}.
 *
 * @author Radoslav Husar
 * @version Jan 2015
 */
public class StopContainerWithTimeout extends ContainerControlEvent {

    private final int timeout;

    /**
     * @param container container to stop
     * @param timeout   graceful shutdown timeout in seconds
     */
    public StopContainerWithTimeout(Container container, int timeout) {
        super(container);
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout;
    }
}

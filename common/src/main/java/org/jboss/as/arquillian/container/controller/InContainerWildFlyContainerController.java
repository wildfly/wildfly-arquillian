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

import org.jboss.arquillian.container.test.impl.client.container.ContainerContainerController;
import org.jboss.as.arquillian.api.WildFlyContainerController;
import org.jboss.as.arquillian.container.controller.command.StopWithTimeoutContainerCommand;

/**
 * {@link org.jboss.arquillian.container.test.api.ContainerController} running in container executing {@link org.jboss.arquillian.container.test.spi.command.Command}s
 * over the {@link org.jboss.arquillian.container.test.spi.command.CommandService}.
 *
 * @author Radoslav Husar
 * @version Jan 2015
 */
public class InContainerWildFlyContainerController extends ContainerContainerController implements WildFlyContainerController {

    @Override
    public void stop(String containerQualifier, int timeout) {
        getCommandService().execute(new StopWithTimeoutContainerCommand(containerQualifier, timeout));
    }

}

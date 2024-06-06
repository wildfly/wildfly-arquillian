/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.controller;

import org.jboss.arquillian.container.test.impl.client.container.ContainerContainerController;
import org.jboss.as.arquillian.api.WildFlyContainerController;
import org.jboss.as.arquillian.container.controller.command.StopWithTimeoutContainerCommand;

/**
 * {@link org.jboss.arquillian.container.test.api.ContainerController} running in container executing
 * {@link org.jboss.arquillian.container.test.spi.command.Command}s
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

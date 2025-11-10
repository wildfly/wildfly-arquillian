/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.controller.command;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.as.arquillian.api.WildFlyContainerController;

/**
 * {@link WildFlyContainerCommandObserver} observing {@link StopWithTimeoutContainerCommand} events.
 *
 * @author Radoslav Husar
 */
public class WildFlyContainerCommandObserver {

    @Inject
    private Instance<WildFlyContainerController> controllerInst;

    public void stop(@Observes StopWithTimeoutContainerCommand event) {
        controllerInst.get().stop(event.getContainerQualifier(), event.getTimeout());
        event.setResult("SUCCESS");
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.controller;

import org.jboss.arquillian.container.spi.event.SetupContainers;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.as.arquillian.api.WildFlyContainerController;

/**
 * Produces instances of WildFlyContainerController when running as client.
 *
 * @author Radoslav Husar
 * @version Jan 2015
 */
public class ClientWildFlyContainerControllerCreator {

    @Inject
    @ApplicationScoped
    private InstanceProducer<WildFlyContainerController> controller;

    @Inject
    private Instance<Injector> injector;

    @SuppressWarnings("UnusedParameters")
    public void create(@Observes SetupContainers event) {
        controller.set(injector.get().inject(new ClientWildFlyContainerController()));
    }
}

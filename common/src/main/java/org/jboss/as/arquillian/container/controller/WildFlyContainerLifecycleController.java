/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.arquillian.container.controller;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.event.container.AfterStop;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.container.spi.event.container.ContainerEvent;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Injector;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.as.arquillian.container.CommonDeployableContainer;

/**
 * WildFlyContainerLifecycleController
 *
 * @author Radoslav Husar
 * @version Jan 2015
 * @see org.jboss.arquillian.container.impl.client.container.ContainerLifecycleController
 */
public class WildFlyContainerLifecycleController {

    @Inject
    private Instance<Injector> injector;

    @Inject
    private Event<ContainerEvent> event;

    @SuppressWarnings("UnusedDeclaration")
    public void stopContainerWithTimeout(@Observes final StopContainerWithTimeout stopEvent) throws Exception {
        forContainer(stopEvent.getContainer(), new Operation<Container>() {
            @Override
            public void perform(Container container) throws Exception {
                event.fire(new BeforeStop(container.getDeployableContainer()));
                try {
                    if (container.getState().equals(Container.State.STARTED)) {
                        CommonDeployableContainer c = (CommonDeployableContainer) container.getDeployableContainer();
                        c.stop(stopEvent.getTimeout());
                    }
                    container.setState(Container.State.STOPPED);
                } catch (LifecycleException e) {
                    container.setState(Container.State.STOPPED_FAILED);
                    throw e;
                }
                event.fire(new AfterStop(container.getDeployableContainer()));
            }
        });
    }

    private void forContainer(Container container, Operation<Container> operation) throws Exception {
        injector.get().inject(operation);
        operation.perform(container);
    }

    public interface Operation<T> {
        void perform(T container) throws Exception;
    }

}

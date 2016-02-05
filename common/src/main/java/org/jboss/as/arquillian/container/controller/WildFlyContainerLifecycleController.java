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

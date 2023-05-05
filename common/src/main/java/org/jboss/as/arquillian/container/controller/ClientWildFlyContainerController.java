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

import java.util.List;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.deployment.TargetDescription;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.container.test.impl.client.container.ClientContainerController;
import org.jboss.as.arquillian.api.WildFlyContainerController;

/**
 * Implementation of {@link WildFlyContainerController} used from client.
 *
 * @author Radoslav Husar
 * @version Jan 2015
 */
public class ClientWildFlyContainerController extends ClientContainerController implements WildFlyContainerController {

    private final Logger log = Logger.getLogger(ClientWildFlyContainerController.class.getName());

    /**
     * @see org.jboss.arquillian.container.test.impl.client.container.ClientContainerController#stop(java.lang.String)
     */
    @Override
    public void stop(String containerQualifier, int timeout) {
        {
            DeploymentScenario scenario = getDeploymentScenario().get();
            if (scenario == null) {
                throw new IllegalArgumentException("No deployment scenario in context");
            }

            ContainerRegistry registry = getContainerRegistry().get();
            if (registry == null) {
                throw new IllegalArgumentException("No container registry in context");
            }

            if (!containerExists(registry.getContainers(), containerQualifier)) {
                throw new IllegalArgumentException("No container with the specified name exists");
            }

            if (!isControllableContainer(registry.getContainers(), containerQualifier)) {
                throw new IllegalArgumentException("Could not start " + containerQualifier
                        + " container. The container life cycle is controlled by Arquillian");
            }

            Container container = getContainerRegistry().get().getContainer(new TargetDescription(containerQualifier));

            List<Deployment> managedDeployments = scenario.startupDeploymentsFor(new TargetDescription(containerQualifier));

            for (Deployment d : managedDeployments) {
                if (d.isDeployed()) {
                    log.info("Automatic undeploying of the managed deployment with name " + d.getDescription().getName() +
                            " from the container with name " + container.getName());
                    getContainerControllerEvent().fire(new UnDeployDeployment(container, d));
                }
            }

            log.info("Manual stopping of a server instance with timeout=" + timeout);

            getContainerControllerEvent().fire(new StopContainerWithTimeout(container, timeout));
        }
    }
}
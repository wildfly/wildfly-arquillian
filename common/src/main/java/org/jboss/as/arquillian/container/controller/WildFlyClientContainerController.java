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
 * WildFlyClientContainerController
 *
 * @author Radoslav Husar
 * @version Jan 2015
 */
public class WildFlyClientContainerController extends ClientContainerController implements WildFlyContainerController {

    private final Logger log = Logger.getLogger(WildFlyClientContainerController.class.getName());

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
                throw new IllegalArgumentException("Could not start " + containerQualifier + " container. The container life cycle is controlled by Arquillian");
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
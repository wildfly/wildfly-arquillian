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
package org.jboss.as.arquillian.container.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.as.controller.client.helpers.domain.DeployDeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.domain.DeploymentAction;
import org.jboss.as.controller.client.helpers.domain.DeploymentPlan;
import org.jboss.as.controller.client.helpers.domain.DeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.domain.DeploymentPlanResult;
import org.jboss.as.controller.client.helpers.domain.DomainDeploymentManager;
import org.jboss.as.controller.client.helpers.domain.InitialDeploymentSetBuilder;
import org.jboss.as.controller.client.helpers.domain.ServerDeploymentPlanResult;
import org.jboss.as.controller.client.helpers.domain.ServerGroupDeploymentPlanBuilder;
import org.jboss.as.controller.client.helpers.domain.ServerGroupDeploymentPlanResult;
import org.jboss.as.controller.client.helpers.domain.ServerUpdateResult;
import org.jboss.as.controller.client.helpers.domain.UndeployDeploymentPlanBuilder;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * A deployer that uses the {@link org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager}
 *
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @since 17-Nov-2010
 */
public class ArchiveDeployer {

    private static final Logger log = Logger.getLogger(ArchiveDeployer.class);

    private final DomainDeploymentManager deploymentManager;

    public ArchiveDeployer(DomainDeploymentManager deploymentManager) {
        this.deploymentManager = deploymentManager;
    }

    public String deploy(Archive<?> archive, String target) throws DeploymentException {
        return deploy(archive, Collections.singleton(target));
    }

    /**
     * Deploys the archive to multiple server groups.
     *
     * @param archive      the archive to deploy
     * @param serverGroups the server groups to deploy to
     *
     * @return a unique identifier for the deployment
     *
     * @throws DeploymentException if an error occurs during deployment
     */
    public String deploy(Archive<?> archive, Set<String> serverGroups) throws DeploymentException {
        if (serverGroups.isEmpty()) {
            throw new DeploymentException("No target server groups to deploy to.");
        }
        try {
            final InputStream input = archive.as(ZipExporter.class).exportAsInputStream();
            try {
                InitialDeploymentSetBuilder builder = deploymentManager.newDeploymentPlan().withRollbackAcrossGroups();
                final DeployDeploymentPlanBuilder deployBuilder = builder.add(archive.getName(), input).andDeploy();
                ServerGroupDeploymentPlanBuilder serverGroupBuilder = null;
                for (String target : serverGroups) {
                    serverGroupBuilder = (serverGroupBuilder == null ? deployBuilder.toServerGroup(target) : serverGroupBuilder.toServerGroup(target));
                }
                DeploymentPlan plan = serverGroupBuilder.build();
                DeploymentAction deployAction = plan.getDeploymentActions().get(plan.getDeploymentActions().size() - 1);
                return executeDeploymentPlan(plan, deployAction);
            } finally {
                if (input != null)
                    try {
                        input.close();
                    } catch (IOException e) {
                        log.warnf(e, "Failed to close resource %s", input);
                    }
            }

        } catch (Exception e) {
            throw new DeploymentException("Could not deploy to container", e);
        }
    }

    public void undeploy(String runtimeName, String target) throws DeploymentException {
        undeploy(runtimeName, Collections.singleton(target));
    }

    /**
     * Undeploys the content specified by the {@code runtimeName} from the server groups.
     *
     * @param runtimeName the name of the deployment
     * @param serverGroups the server groups to deploy to
     *
     * @throws DeploymentException if an error occurs during undeployment
     */
    public void undeploy(String runtimeName, Set<String> serverGroups) throws DeploymentException {
        if (serverGroups.isEmpty()) {
            throw new DeploymentException("No target server groups to deploy to.");
        }
        try {
            DeploymentPlanBuilder builder = deploymentManager.newDeploymentPlan();
            UndeployDeploymentPlanBuilder undeployBuilder = builder.undeploy(runtimeName);
            ServerGroupDeploymentPlanBuilder serverGroupBuilder = null;
            for (String target : serverGroups) {
                serverGroupBuilder = (serverGroupBuilder == null ? undeployBuilder.toServerGroup(target) : serverGroupBuilder.toServerGroup(target));
            }
            DeploymentPlan plan = serverGroupBuilder.build();
            Future<DeploymentPlanResult> future = deploymentManager.execute(plan);
            future.get();
        } catch (Exception ex) {
            log.warn("Cannot undeploy: " + runtimeName + ":" + ex.getMessage());
        }
    }

    private String executeDeploymentPlan(DeploymentPlan plan, DeploymentAction deployAction) throws Exception {
        Future<DeploymentPlanResult> future = deploymentManager.execute(plan);
        DeploymentPlanResult planResult = future.get();

        Map<String, ServerGroupDeploymentPlanResult> actionResults = planResult.getServerGroupResults();
        for (Entry<String, ServerGroupDeploymentPlanResult> result : actionResults.entrySet()) {
            for (Entry<String, ServerDeploymentPlanResult> planServerResult : result.getValue().getServerResults().entrySet()) {
                ServerUpdateResult actionResult = planServerResult.getValue().getDeploymentActionResults()
                        .get(deployAction.getId());
                if (actionResult != null) {
                    Exception deploymentException = (Exception) actionResult.getFailureResult();
                    if (deploymentException != null)
                        throw deploymentException;
                }
            }
        }

        return deployAction.getDeploymentUnitUniqueName();
    }
}
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
import org.wildfly.common.Assert;
import org.wildfly.plugin.core.Deployment;
import org.wildfly.plugin.core.DeploymentManager;
import org.wildfly.plugin.core.DeploymentResult;
import org.wildfly.plugin.core.UndeployDescription;

/**
 * Allows deployment operations to be executed on a running server.
 *
 * <p>
 * The client is not closed by an instance of this and is the responsibility of the user to clean up the client instance.
 * </p>
 *
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @since 17-Nov-2010
 */
@SuppressWarnings({"WeakerAccess", "TypeMayBeWeakened", "DeprecatedIsStillUsed", "deprecation", "unused"})
public class ArchiveDeployer {

    private static final Logger log = Logger.getLogger(ArchiveDeployer.class);

    // This should be removed at some point, but for compatibility we'll keep it
    @Deprecated
    private final DomainDeploymentManager deploymentManagerDeprecated;
    private final DeploymentManager deploymentManager;

    /**
     * Creates a new deployer.
     *
     * @param deploymentManager the deployment manager to use
     *
     * @see #ArchiveDeployer(ManagementClient)
     * @deprecated the {@link DomainDeploymentManager} will no longer be used in future releases, use the
     * {@link #ArchiveDeployer(ManagementClient)} constructor
     */
    @Deprecated
    public ArchiveDeployer(DomainDeploymentManager deploymentManager) {
        Assert.checkNotNullParam("deploymentManager", deploymentManager);
        this.deploymentManagerDeprecated = deploymentManager;
        this.deploymentManager = null;
    }

    /**
     * Creates a new deployer.
     *
     * @param client the client used to communicate with the server
     */
    public ArchiveDeployer(final ManagementClient client) {
        Assert.checkNotNullParam("client", client);
        deploymentManagerDeprecated = null;
        this.deploymentManager = DeploymentManager.Factory.create(client.getControllerClient());
    }

    /**
     * Deploys the archive to the server group.
     *
     * @param archive     the archive to deploy
     * @param serverGroup the server group to deploy to
     *
     * @return a unique identifier for the deployment
     *
     * @throws DeploymentException if an error occurs during deployment
     */
    public String deploy(Archive<?> archive, String serverGroup) throws DeploymentException {
        return deploy(archive, Collections.singleton(serverGroup));
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
            // If a deployment manager is available use it, otherwise default to the previous behavior
            if (deploymentManager != null) {
                final String name = archive.getName();
                final DeploymentResult result = deploymentManager.deploy(Deployment.of(input, name)
                        .setServerGroups(serverGroups));
                if (!result.successful()) {
                    throw new DeploymentException("Could not deploy to container: " + result.getFailureMessage());
                }
                return name;
            } else {
                // Fallback behavior if constructed with a DomainDeploymentManager
                try {
                    InitialDeploymentSetBuilder builder = deploymentManagerDeprecated.newDeploymentPlan().withRollbackAcrossGroups();
                    final DeployDeploymentPlanBuilder deployBuilder = builder.add(archive.getName(), input).andDeploy();
                    ServerGroupDeploymentPlanBuilder serverGroupBuilder = null;
                    for (String target : serverGroups) {
                        serverGroupBuilder = (serverGroupBuilder == null ? deployBuilder.toServerGroup(target) : serverGroupBuilder.toServerGroup(target));
                    }
                    @SuppressWarnings("ConstantConditions")
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
            }

        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException("Could not deploy to container", e);
        }
    }

    /**
     * Undeploys the content specified by the {@code runtimeName} from the server group.
     *
     * @param runtimeName the name of the deployment
     * @param serverGroup the server group to undeploy to
     *
     * @throws DeploymentException if an error occurs during undeployment
     */
    public void undeploy(final String runtimeName, final String serverGroup) throws DeploymentException {
        undeploy(runtimeName, Collections.singleton(serverGroup));
    }

    /**
     * Undeploys the content specified by the {@code runtimeName} from the server groups.
     *
     * @param runtimeName  the name of the deployment
     * @param serverGroups the server groups to undeploy to
     *
     * @throws DeploymentException if an error occurs during undeployment
     */
    public void undeploy(String runtimeName, Set<String> serverGroups) throws DeploymentException {
        if (serverGroups.isEmpty()) {
            throw new DeploymentException("No target server groups to deploy to.");
        }
        try {
            if (deploymentManager != null) {
                final DeploymentResult result = deploymentManager.undeploy(UndeployDescription.of(runtimeName)
                        .addServerGroups(serverGroups));
                if (!result.successful()) {
                    log.warnf("Cannot undeploy %s: %s", runtimeName, result.getFailureMessage());
                }
            } else {
                DeploymentPlanBuilder builder = deploymentManagerDeprecated.newDeploymentPlan();
                UndeployDeploymentPlanBuilder undeployBuilder = builder.undeploy(runtimeName);
                ServerGroupDeploymentPlanBuilder serverGroupBuilder = null;
                for (String target : serverGroups) {
                    serverGroupBuilder = (serverGroupBuilder == null ? undeployBuilder.toServerGroup(target) : serverGroupBuilder.toServerGroup(target));
                }
                @SuppressWarnings("ConstantConditions")
                DeploymentPlan plan = serverGroupBuilder.build();
                Future<DeploymentPlanResult> future = deploymentManagerDeprecated.execute(plan);
                future.get();
            }
        } catch (Exception ex) {
            log.warnf("Cannot undeploy %s: %s", runtimeName, ex.getLocalizedMessage());
        }
    }

    /**
     * Checks if the deployment content is on the server.
     *
     * @param name the name of the deployment
     *
     * @return {@code true} if the deployment content exists otherwise {@code false}
     *
     * @throws IOException if a failure occurs communicating with the server
     */
    public boolean hasDeployment(final String name) throws IOException {
        return deploymentManager.hasDeployment(name);
    }

    /**
     * Checks if the deployment content is on the server.
     *
     * @param name        the name of the deployment
     * @param serverGroup the server group to check for the deployment on
     *
     * @return {@code true} if the deployment content exists otherwise {@code false}
     *
     * @throws IOException if a failure occurs communicating with the server
     */
    public boolean hasDeployment(final String name, final String serverGroup) throws IOException {
        return deploymentManager.hasDeployment(name, serverGroup);
    }

    private String executeDeploymentPlan(DeploymentPlan plan, DeploymentAction deployAction) throws Exception {
        Future<DeploymentPlanResult> future = deploymentManagerDeprecated.execute(plan);
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
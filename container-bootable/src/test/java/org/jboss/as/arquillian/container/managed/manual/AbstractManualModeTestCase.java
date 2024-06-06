/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed.manual;

import java.io.IOException;
import java.util.Map;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.WildFlyContainerController;
import org.jboss.as.arquillian.container.ArchiveDeployer;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public abstract class AbstractManualModeTestCase {
    private static final ModelNode EMPTY_ADDRESS = new ModelNode().setEmptyList();

    @ArquillianResource
    static ContainerController controller;

    @Deployment(name = "dep1")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(AbstractManualModeTestCase.class, ArchiveDeployer.class, WildFlyContainerController.class,
                        ManagementClient.class)
                .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                        + "Dependencies: org.jboss.as.controller-client,org.jboss.dmr\n"))
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @After
    public void stop() throws Exception {
        if (controller.isStarted(containerName())) {
            controller.stop(containerName());
        }
    }

    @Test
    public void serverControl() {
        final String containerName = containerName();
        // The primary container should already be started
        controller.start(containerName, createConfig("server-control"));
        Assert.assertTrue(String.format("The container \"%s\" should be started", containerName),
                controller.isStarted(containerName));
        controller.stop(containerName);
        Assert.assertFalse(String.format("The container \"%s\" should be stopped", containerName),
                controller.isStarted(containerName));
    }

    @Test
    public void managementClient() throws Exception {
        final String containerName = containerName();
        controller.start(containerName, createConfig("management-client"));
        executeForSuccess(client(), Operations.createReadAttributeOperation(EMPTY_ADDRESS, "server-state"));
        controller.stop(containerName);
    }

    @Test
    public void deploy() throws Exception {
        final String containerName = containerName();
        controller.start(containerName, createConfig("deploy"));
        final int currentDeployments = getCurrentDeploymentCount(client());
        // Deploy both deployments
        try {
            deployer().deploy(createDeployment());
            // Read each result, we should have two results for the first op and one for the second
            final int newDeployments = getCurrentDeploymentCount(client());
            Assert.assertEquals("Expected 1 deployments found " + (newDeployments - currentDeployments) + " for container "
                    + containerName, newDeployments, (1 + currentDeployments));
        } finally {
            deployer().undeploy("dep1");
            controller.stop(containerName);
        }
    }

    protected abstract String containerName();

    protected abstract ManagementClient client();

    protected abstract ArchiveDeployer deployer();

    protected Map<String, String> createConfig(final String suffix) {
        return Map.of();
    }

    static ModelNode executeForSuccess(final ManagementClient client, final ModelNode op) throws IOException {
        return executeForSuccess(client, Operation.Factory.create(op));
    }

    static ModelNode executeForSuccess(final ManagementClient client, final Operation op) throws IOException {
        final ModelNode result = client.getControllerClient().execute(op);
        if (Operations.isSuccessfulOutcome(result)) {
            return Operations.readResult(result);
        }
        Assert.fail(
                String.format("Failed to execute operation: %s%n%s", op, Operations.getFailureDescription(result)
                        .asString()));
        return new ModelNode();
    }

    static int getCurrentDeploymentCount(final ManagementClient client) throws IOException {
        final ModelNode op = Operations.createOperation(ClientConstants.READ_CHILDREN_NAMES_OPERATION, EMPTY_ADDRESS);
        op.get(ClientConstants.CHILD_TYPE).set(ClientConstants.DEPLOYMENT);
        return executeForSuccess(client, op).asList().size();
    }

}

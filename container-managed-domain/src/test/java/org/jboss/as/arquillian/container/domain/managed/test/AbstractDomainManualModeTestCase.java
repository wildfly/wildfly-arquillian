/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.domain.managed.test;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.domain.ManagementClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.wildfly.arquillian.domain.api.DomainContainerController;
import org.wildfly.arquillian.domain.api.TargetsServerGroup;

/**
 * For Domain server DeployableContainer implementations, the DeployableContainer will register
 * all groups/individual servers it controls as Containers in Arquillian's Registry during start.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
public abstract class AbstractDomainManualModeTestCase {

    @ArquillianResource
    static DomainContainerController controller;

    @Deployment(managed = false, name = "dep1")
    @TargetsServerGroup("main-server-group")
    @TargetsServerGroup("other-server-group")
    public static WebArchive create1() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Deployment(managed = false, testable = false, name = "dep2")
    @TargetsServerGroup("main-server-group")
    public static WebArchive create2() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testServerControl() throws Exception {
        final String hostName = client().getLocalHostName();
        final String serverName = "server-two";
        final String containerName = containerName();
        controller.stopServer(containerName, hostName, serverName);
        Assert.assertFalse("server-two on host " + hostName + " should not be started",
                controller.isServerStarted(containerName, hostName, serverName));

        // Attempt to start server-two
        controller.startServer(containerName, hostName, serverName);
        Assert.assertTrue("server-two should not be started on host " + hostName + ", but was not",
                controller.isServerStarted(containerName, hostName, serverName));
    }

    @Test
    public void testServerGroupControl() throws Exception {
        final String serverGroupName = "main-server-group";
        final String containerName = containerName();
        controller.stopServers(containerName, serverGroupName);
        final String hostName = client().getLocalHostName();

        // The servers should all be stopped
        for (String serverName : getServerGroupServers(serverGroupName)) {
            Assert.assertFalse(String.format("Server %s should be stopped on host %s - server group %s", serverName, hostName,
                    serverGroupName), controller.isServerStarted(containerName, hostName, serverName));
        }
        controller.startServers(containerName, serverGroupName);
        for (String serverName : getServerGroupServers(serverGroupName)) {
            Assert.assertTrue(String.format("Server %s should be stopped on host %s - server group %s", serverName, hostName,
                    serverGroupName), controller.isServerStarted(containerName, hostName, serverName));
        }
        TimeUnit.SECONDS.sleep(2L);
    }

    @Test
    public void testDeploy() throws Exception {
        final int currentMainServerGroupDeployments = getCurrentDeploymentCount("main-server-group");
        final int currentOtherServerGroupDeployments = getCurrentDeploymentCount("other-server-group");
        // Deploy both deployments
        final Deployer deployer = deployer();
        try {
            deployer.deploy("dep1");
            deployer.deploy("dep2");
            final Operations.CompositeOperationBuilder builder = Operations.CompositeOperationBuilder.create();

            ModelNode op = Operations.createOperation(ClientConstants.READ_CHILDREN_NAMES_OPERATION,
                    Operations.createAddress(ClientConstants.SERVER_GROUP, "main-server-group"));
            op.get(ClientConstants.CHILD_TYPE).set(ClientConstants.DEPLOYMENT);
            builder.addStep(op);

            op = Operations.createOperation(ClientConstants.READ_CHILDREN_NAMES_OPERATION,
                    Operations.createAddress(ClientConstants.SERVER_GROUP, "other-server-group"));
            op.get(ClientConstants.CHILD_TYPE).set(ClientConstants.DEPLOYMENT);
            builder.addStep(op);

            ModelNode result = executeForSuccess(builder.build());
            // Read each result, we should have two results for the first op and one for the second
            final List<ModelNode> step1Result = Operations.readResult(result.get("step-1")).asList();
            Assert.assertTrue("Expected 2 deployments found " + (step1Result.size() - currentMainServerGroupDeployments),
                    step1Result.size() == (2 + currentMainServerGroupDeployments));
            final List<ModelNode> step2Result = Operations.readResult(result.get("step-2")).asList();
            Assert.assertTrue("Expected 1 deployments found " + (step2Result.size() - currentOtherServerGroupDeployments),
                    step2Result.size() == (1 + currentOtherServerGroupDeployments));
        } finally {
            deployer.undeploy("dep1");
            deployer.undeploy("dep2");
        }
    }

    protected abstract String containerName();

    protected abstract ManagementClient client();

    protected abstract Deployer deployer();

    ModelNode executeForSuccess(final ModelNode op) throws IOException {
        return executeForSuccess(Operation.Factory.create(op));
    }

    ModelNode executeForSuccess(final Operation op) throws IOException {
        final ModelNode result = client().getControllerClient().execute(op);
        if (Operations.isSuccessfulOutcome(result)) {
            return Operations.readResult(result);
        }
        Assert.fail(
                String.format("Failed to execute operation: %s%n%s", op, Operations.getFailureDescription(result).asString()));
        return new ModelNode();
    }

    protected Set<String> getServerGroupServers(final String name) throws IOException {
        final Set<String> servers = new LinkedHashSet<>();
        final ModelNode address = Operations.createAddress(ClientConstants.HOST, "*", ClientConstants.SERVER_CONFIG, "*");
        final ModelNode op = Operations.createReadAttributeOperation(address, "group");
        final ModelNode result = executeForSuccess(op);
        for (ModelNode n : result.asList()) {
            // Get the address and parse it out as we only need the two values
            final List<ModelNode> segments = Operations.getOperationAddress(n).asList();
            // Should be at least two address segments
            if (segments.size() >= 2) {
                final String serverGroupName = Operations.readResult(n).asString();
                if (name.equals(serverGroupName)) {
                    servers.add(segments.get(1).get(ClientConstants.SERVER_CONFIG).asString());
                }
            }
        }
        return Collections.unmodifiableSet(servers);
    }

    protected int getCurrentDeploymentCount(final String serverGroupName) throws IOException {
        final ModelNode op = Operations.createOperation(ClientConstants.READ_CHILDREN_NAMES_OPERATION,
                Operations.createAddress(ClientConstants.SERVER_GROUP, serverGroupName));
        op.get(ClientConstants.CHILD_TYPE).set(ClientConstants.DEPLOYMENT);
        return executeForSuccess(op).asList().size();
    }

}

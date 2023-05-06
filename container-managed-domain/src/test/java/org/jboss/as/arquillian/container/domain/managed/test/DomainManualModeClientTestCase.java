/*
 * Copyright 2016 Red Hat, Inc.
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
package org.jboss.as.arquillian.container.domain.managed.test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.client.helpers.Operations.CompositeOperationBuilder;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.arquillian.domain.api.DomainContainerController;

/**
 * For Domain server DeployableContainer implementations, the DeployableContainer will register
 * all groups/individual servers it controls as Containers in Arquillian's Registry during start.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class DomainManualModeClientTestCase extends AbstractDomainManualModeTestCase {

    @Test
    public void testServerGroupControl() throws Exception {
        final String serverGroupName = "main-server-group";
        controller.stopServers(PRIMARY_CONTAINER, serverGroupName);
        final String hostName = client.getLocalHostName();

        // The servers should all be stopped
        for (String serverName : getServerGroupServers(serverGroupName)) {
            Assert.assertFalse(String.format("Server %s should be stopped on host %s - server group %s", serverName, hostName,
                    serverGroupName), controller.isServerStarted(PRIMARY_CONTAINER, hostName, serverName));
        }
        controller.startServers(PRIMARY_CONTAINER, serverGroupName);
        for (String serverName : getServerGroupServers(serverGroupName)) {
            Assert.assertTrue(String.format("Server %s should be stopped on host %s - server group %s", serverName, hostName,
                    serverGroupName), controller.isServerStarted(PRIMARY_CONTAINER, hostName, serverName));
        }
        TimeUnit.SECONDS.sleep(2L);
    }

    @Test
    public void testDeploy() throws Exception {
        final int currentMainServerGroupDeployments = getCurrentDeploymentCount("main-server-group");
        final int currentOtherServerGroupDeployments = getCurrentDeploymentCount("other-server-group");
        // Deploy both deployments
        try {
            deployer.deploy("dep1");
            deployer.deploy("dep2");
            final CompositeOperationBuilder builder = CompositeOperationBuilder.create();

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

    @Test
    public void testSecondaryDomainContainerController(
            @ArquillianResource @TargetsContainer(SECONDARY_CONTAINER) DomainContainerController controller) throws Exception {
        // Ensure the default server is stopped
        stop();

        // Start the wildfly container
        try {
            controller.start(SECONDARY_CONTAINER);
            final String hostName = client.getLocalHostName();
            final String serverName = "server-one";

            controller.stopServer(SECONDARY_CONTAINER, hostName, serverName);
            Assert.assertFalse("server-one on host " + hostName + " should not be started",
                    controller.isServerStarted(SECONDARY_CONTAINER, hostName, serverName));

            // Attempt to start server-one
            controller.startServer(SECONDARY_CONTAINER, hostName, serverName);
            Assert.assertTrue("server-one should not be started on host " + hostName + ", but was not",
                    controller.isServerStarted(SECONDARY_CONTAINER, hostName, serverName));
        } finally {
            controller.stop(SECONDARY_CONTAINER);
        }
    }
}

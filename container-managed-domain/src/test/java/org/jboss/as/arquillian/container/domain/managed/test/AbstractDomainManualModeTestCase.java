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

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.domain.ManagementClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.wildfly.arquillian.domain.api.DomainContainerController;
import org.wildfly.arquillian.domain.api.TargetsServerGroup;

/**
 * For Domain server DeployableContainer implementations, the DeployableContainer will register
 * all groups/individual servers it controls as Containers in Arquillian's Registry during start.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
@Category(ManualMode.class)
public abstract class AbstractDomainManualModeTestCase {
    static final String PRIMARY_CONTAINER = "jboss";
    static final String SECONDARY_CONTAINER = "wildfly";

    @ArquillianResource
    static DomainContainerController controller;

    @ArquillianResource
    @TargetsContainer(PRIMARY_CONTAINER)
    ManagementClient client;

    @ArquillianResource
    Deployer deployer;

    @Deployment(managed = false, name = "dep1")
    @TargetsServerGroup("main-server-group")
    @TargetsServerGroup("other-server-group")
    public static WebArchive create1() {
        return ShrinkWrap.create(WebArchive.class)
                // Required for JUnit when running in ARQ
                .addClass(ManualMode.class);
    }

    @Deployment(managed = false, testable = false, name = "dep2")
    @TargetsServerGroup("main-server-group")
    public static WebArchive create2() {
        return ShrinkWrap.create(WebArchive.class)
                // Required for JUnit when running in ARQ
                .addClass(ManualMode.class);
    }

    @AfterClass
    public static void stop() throws Exception {
        if (controller.isStarted(PRIMARY_CONTAINER)) {
            controller.stop(PRIMARY_CONTAINER);
        }
    }

    @Before
    public void startOnce() throws Exception {
        if (!controller.isStarted(PRIMARY_CONTAINER)) {
            controller.start(PRIMARY_CONTAINER);
        }
    }

    @Test
    public void testServerControl() throws Exception {
        final String hostName = "master";
        final String serverName = "server-two";
        controller.stopServer(PRIMARY_CONTAINER, hostName, serverName);
        Assert.assertFalse("server-two on host master should not be started", controller.isServerStarted(PRIMARY_CONTAINER, hostName, serverName));

        // Attempt to start server-two
        controller.startServer(PRIMARY_CONTAINER, hostName, serverName);
        Assert.assertTrue("server-two should not be started on host master, but was not", controller.isServerStarted(PRIMARY_CONTAINER, hostName, serverName));
    }

    ModelNode executeForSuccess(final ModelNode op) throws IOException {
        return executeForSuccess(Operation.Factory.create(op));
    }

    ModelNode executeForSuccess(final Operation op) throws IOException {
        final ModelNode result = client.getControllerClient().execute(op);
        if (Operations.isSuccessfulOutcome(result)) {
            return Operations.readResult(result);
        }
        Assert.fail(String.format("Failed to execute operation: %s%n%s", op, Operations.getFailureDescription(result).asString()));
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
        final ModelNode op = Operations.createOperation(ClientConstants.READ_CHILDREN_NAMES_OPERATION, Operations.createAddress(ClientConstants.SERVER_GROUP, serverGroupName));
        op.get(ClientConstants.CHILD_TYPE).set(ClientConstants.DEPLOYMENT);
        return executeForSuccess(op).asList().size();
    }

}

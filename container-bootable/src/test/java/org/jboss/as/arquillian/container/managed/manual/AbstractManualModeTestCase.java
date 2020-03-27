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

package org.jboss.as.arquillian.container.managed.manual;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Category(ManualMode.class)
public abstract class AbstractManualModeTestCase {
    private static final ModelNode EMPTY_ADDRESS = new ModelNode().setEmptyList();
    static final String PRIMARY_CONTAINER = "jboss";
    static final String SECONDARY_CONTAINER = "wildfly";

    @ArquillianResource
    static ContainerController controller;

    @ArquillianResource
    @TargetsContainer(PRIMARY_CONTAINER)
    ManagementClient primaryClient;

    @ArquillianResource
    @TargetsContainer(SECONDARY_CONTAINER)
    ManagementClient secondaryClient;

    @Deployment(managed = false, name = "dep1")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                // Required for JUnit when running in ARQ
                .addClass(ManualMode.class);
    }

    @After
    public void stop() throws Exception {
        if (controller.isStarted(PRIMARY_CONTAINER)) {
            controller.stop(PRIMARY_CONTAINER);
        }
        if (controller.isStarted(SECONDARY_CONTAINER)) {
            controller.stop(SECONDARY_CONTAINER);
        }
    }

    @Test
    public void testServerControl() throws Exception {
        // The primary container should already be started
        controller.start(PRIMARY_CONTAINER);
        Assert.assertTrue(String.format("The container \"%s\" should be started", PRIMARY_CONTAINER), controller.isStarted(PRIMARY_CONTAINER));
        controller.stop(PRIMARY_CONTAINER);
        Assert.assertFalse(String.format("The container \"%s\" should be stopped", PRIMARY_CONTAINER), controller.isStarted(PRIMARY_CONTAINER));

        // Start and stop the secondary controller
        controller.start(SECONDARY_CONTAINER);
        Assert.assertTrue(String.format("The container \"%s\" should be started", SECONDARY_CONTAINER), controller.isStarted(SECONDARY_CONTAINER));
        controller.stop(SECONDARY_CONTAINER);
        Assert.assertFalse(String.format("The container \"%s\" should be stopped", SECONDARY_CONTAINER), controller.isStarted(SECONDARY_CONTAINER));
    }

    @Test
    public void testManagementClient() throws Exception {
        controller.start(PRIMARY_CONTAINER);
        executeForSuccess(primaryClient, Operations.createReadAttributeOperation(EMPTY_ADDRESS, "server-state"));
        controller.stop(PRIMARY_CONTAINER);
        controller.start(SECONDARY_CONTAINER);
        executeForSuccess(secondaryClient, Operations.createReadAttributeOperation(EMPTY_ADDRESS, "server-state"));
        controller.stop(SECONDARY_CONTAINER);
    }

    static ModelNode executeForSuccess(final ManagementClient client, final ModelNode op) throws IOException {
        return executeForSuccess(client, Operation.Factory.create(op));
    }

    static ModelNode executeForSuccess(final ManagementClient client, final Operation op) throws IOException {
        final ModelNode result = client.getControllerClient().execute(op);
        if (Operations.isSuccessfulOutcome(result)) {
            return Operations.readResult(result);
        }
        Assert.fail(String.format("Failed to execute operation: %s%n%s", op, Operations.getFailureDescription(result).asString()));
        return new ModelNode();
    }

    static int getCurrentDeploymentCount(final ManagementClient client) throws IOException {
        final ModelNode op = Operations.createOperation(ClientConstants.READ_CHILDREN_NAMES_OPERATION, EMPTY_ADDRESS);
        op.get(ClientConstants.CHILD_TYPE).set(ClientConstants.DEPLOYMENT);
        return executeForSuccess(client, op).asList().size();
    }

}

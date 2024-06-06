/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed.manual;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Category(ManualMode.class)
@RunWith(Arquillian.class)
@RunAsClient
public class ReadOnlyServerConfigTestCase {
    private static final String DEFAULT_CONTAINER_ID = "jboss";
    private static final String READ_ONLY_CONTAINER_ID = "read-only-config";

    @ArquillianResource
    @SuppressWarnings({ "unused", "StaticVariableMayNotBeInitialized" })
    private static ContainerController controller;

    @ArquillianResource
    @SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
    @TargetsContainer(READ_ONLY_CONTAINER_ID)
    private ManagementClient readOnlyClient;

    @ArquillianResource
    @SuppressWarnings({ "unused", "InstanceVariableMayNotBeInitialized" })
    @TargetsContainer(DEFAULT_CONTAINER_ID)
    private ManagementClient defaultClient;

    @Deployment(managed = false, name = "dep1")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                // Required for JUnit when running in ARQ
                .addClass(ManualMode.class);
    }

    @After
    public void shutdown() {
        if (controller.isStarted(READ_ONLY_CONTAINER_ID)) {
            controller.stop(READ_ONLY_CONTAINER_ID);
        }
        if (controller.isStarted(DEFAULT_CONTAINER_ID)) {
            controller.stop(DEFAULT_CONTAINER_ID);
        }
    }

    @Test
    public void testReadOnlyConfig() throws Exception {
        controller.start(READ_ONLY_CONTAINER_ID);

        // Add a system property which should only be added to the runtime
        final ModelNode address = Operations.createAddress("system-property", "test-read-only-config");
        ModelNode op = Operations.createAddOperation(address);
        op.get("value").set("true");
        executeOperation(readOnlyClient, op);
        op = Operations.createReadResourceOperation(address);
        executeOperation(readOnlyClient, op, false);

        // Stop the container and restart it, the system property should not exist
        controller.stop(READ_ONLY_CONTAINER_ID);
        controller.start(READ_ONLY_CONTAINER_ID);
        executeOperation(readOnlyClient, op, true);
    }

    @Test
    public void testDefault() throws Exception {
        controller.start(DEFAULT_CONTAINER_ID);

        // Add a system property which should only be added to the runtime
        final ModelNode address = Operations.createAddress("system-property", "test-default");
        ModelNode op = Operations.createAddOperation(address);
        op.get("value").set("true");
        executeOperation(defaultClient, op);
        op = Operations.createReadResourceOperation(address);
        executeOperation(defaultClient, op);

        // Stop the container and restart it, the system property should exist
        controller.stop(DEFAULT_CONTAINER_ID);
        controller.start(DEFAULT_CONTAINER_ID);
        executeOperation(defaultClient, op);

        // Remove the system property
        executeOperation(defaultClient, Operations.createRemoveOperation(address));
    }

    private static void executeOperation(final ManagementClient client, final ModelNode op) throws IOException {
        executeOperation(client, op, false);
    }

    private static void executeOperation(final ManagementClient client, final ModelNode op, final boolean expectFailure)
            throws IOException {
        final ModelNode result = client.getControllerClient().execute(op);
        if (expectFailure) {
            Assert.assertFalse(String.format("Expected operation %s to fail: %n%s", op, result),
                    Operations.isSuccessfulOutcome(result));
        } else {
            if (!Operations.isSuccessfulOutcome(result)) {
                Assert.fail(Operations.getFailureDescription(result).asString());
            }
        }
    }
}

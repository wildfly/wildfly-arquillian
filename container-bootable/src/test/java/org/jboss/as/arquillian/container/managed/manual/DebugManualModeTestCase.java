/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.arquillian.container.managed.manual;

import java.io.IOException;
import java.util.List;

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

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Category(ManualMode.class)
@RunWith(Arquillian.class)
@RunAsClient
public class DebugManualModeTestCase {
    private static final String DEBUG_SUSPEND_CONTAINER_ID = "debug-config";

    @ArquillianResource
    private static ContainerController controller;

    @ArquillianResource
    @TargetsContainer(DEBUG_SUSPEND_CONTAINER_ID)
    private ManagementClient debugSuspendClient;

    @Deployment(managed = false, name = "dep1")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                // Required for JUnit when running in ARQ
                .addClass(ManualMode.class);
    }

    @After
    public void shutdown() {
        if (controller.isStarted(DEBUG_SUSPEND_CONTAINER_ID)) {
            controller.stop(DEBUG_SUSPEND_CONTAINER_ID);
        }
    }

    @Test
    public void debugConfig() throws Exception {
        controller.start(DEBUG_SUSPEND_CONTAINER_ID);

        // Attach a debugger
        final VirtualMachine vm = attachDebugger();
        try {
            Assert.assertFalse("VM should be able to see all threads: " + vm, vm.allThreads().isEmpty());
            // Check the server-state
            final ModelNode address = new ModelNode().setEmptyList();
            final ModelNode op = Operations.createReadAttributeOperation(address, "server-state");
            final ModelNode result = executeOperation(debugSuspendClient, op);
            Assert.assertEquals("running", result.asString());
        } finally {
            vm.dispose();
        }
    }

    private static ModelNode executeOperation(final ManagementClient client, final ModelNode op) throws IOException {
        final ModelNode result = client.getControllerClient().execute(op);
        if (!Operations.isSuccessfulOutcome(result)) {
            Assert.fail(Operations.getFailureDescription(result).asString());
        }
        return Operations.readResult(result);
    }

    private static VirtualMachine attachDebugger() throws IllegalConnectorArgumentsException, IOException {
        final var manager = Bootstrap.virtualMachineManager();
        final AttachingConnector connector = findSocket(manager.attachingConnectors());
        Assert.assertNotNull("Failed to find socket connector", connector);
        final var defaultArguments = connector.defaultArguments();
        defaultArguments.get("port").setValue(System.getProperty("test.debug.port", "5005"));
        return connector.attach(defaultArguments);
    }

    private static AttachingConnector findSocket(final List<AttachingConnector> connectors) {
        // Attempt to find the socket connector and configure it
        for (AttachingConnector connector : connectors) {
            if (connector.defaultArguments().containsKey("port")) {
                return connector;
            }
        }
        return null;
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed.manual;

import java.io.IOException;
import java.util.List;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Tag("ManualMode")
@ExtendWith(ArquillianExtension.class)
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

    @AfterEach
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
            Assertions.assertFalse(vm.allThreads().isEmpty(), "VM should be able to see all threads: " + vm);
            // Check the server-state
            final ModelNode address = new ModelNode().setEmptyList();
            final ModelNode op = Operations.createReadAttributeOperation(address, "server-state");
            final ModelNode result = executeOperation(debugSuspendClient, op);
            Assertions.assertEquals("running", result.asString());
        } finally {
            vm.dispose();
        }
    }

    private static ModelNode executeOperation(final ManagementClient client, final ModelNode op) throws IOException {
        final ModelNode result = client.getControllerClient().execute(op);
        if (!Operations.isSuccessfulOutcome(result)) {
            Assertions.fail(Operations.getFailureDescription(result).asString());
        }
        return Operations.readResult(result);
    }

    private static VirtualMachine attachDebugger() throws IllegalConnectorArgumentsException, IOException {
        final var manager = Bootstrap.virtualMachineManager();
        final AttachingConnector connector = findSocket(manager.attachingConnectors());
        Assertions.assertNotNull(connector, "Failed to find socket connector");
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

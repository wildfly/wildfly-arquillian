/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.embedded;

import java.util.ArrayDeque;
import java.util.Deque;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.plugin.tools.server.ServerManager;

/**
 * Tests basic deployment
 */
@ExtendWith(ArquillianExtension.class)
@ServerSetup(DeploymentTestCase.SystemPropertySetupTask.class)
public class DeploymentTestCase {

    public static class SystemPropertySetupTask implements ServerSetupTask {
        private final Deque<ModelNode> tearDownOps = new ArrayDeque<>();

        @ArquillianResource
        private ServerManager serverManager;

        @Override
        public void setup(final ManagementClient managementClient, final String containerId) throws Exception {
            ModelNode address = Operations.createAddress("system-property", TEST_PROPERTY);
            ModelNode op = Operations.createAddOperation(address);
            op.get("value").set(VALUE);
            tearDownOps.add(Operations.createRemoveOperation(address));
            serverManager.executeOperation(op);
        }

        @Override
        public void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
            ModelNode removeOp;
            while ((removeOp = tearDownOps.poll()) != null) {
                serverManager.executeOperation(removeOp);
            }
        }
    }

    private static final String TEST_PROPERTY = "test-property";
    private static final String VALUE = "set";

    @Deployment
    public static JavaArchive create() {
        return ShrinkWrap.create(JavaArchive.class, "test.jar").addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testSystemPropSet() throws Exception {
        Assertions.assertEquals(VALUE, System.getProperty(TEST_PROPERTY));
    }

    @Test
    @RunAsClient
    public void systemPropertyOpAsClient(@ArquillianResource final ServerManager serverManager) throws Exception {
        final ModelNode address = Operations.createAddress("system-property", TEST_PROPERTY);
        Assertions.assertEquals(VALUE,
                serverManager.executeOperation(Operations.createReadAttributeOperation(address, "value")).asString(null));
    }

    @Test
    public void systemPropertyOp() throws Exception {
        try (ModelControllerClient client = ModelControllerClient.Factory.create("localhost", 9990)) {
            final ModelNode address = Operations.createAddress("system-property", TEST_PROPERTY);
            final ModelNode op = Operations.createReadAttributeOperation(address, "value");
            final ModelNode result = client.execute(op);
            Assertions.assertTrue(Operations.isSuccessfulOutcome(result), () -> String.format("Operation failed %s", result));
            Assertions.assertEquals(VALUE, Operations.readResult(result).asString(null));
        }
    }

    @Test
    public void testSystemPropertyArgument() throws Exception {
        testSystemProperty("deployment.arq.test.property");
        testSystemProperty("deployment.arq.other.test.property");
    }

    private static void testSystemProperty(final String key) {
        Assertions.assertNotNull(System.getProperty(key), String.format("Expected a value for property \"%s\"", key));
    }
}

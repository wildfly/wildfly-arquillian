/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests basic deployment
 */
@ExtendWith(ArquillianExtension.class)
@ServerSetup(UnmanagedDeploymentTestCase.SystemPropertyServerSetup.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RunAsClient
public class UnmanagedDeploymentTestCase extends TestOperations {

    private static final String KEY = "test.property.key";
    private static final String VALUE = "test-value";

    static class SystemPropertyServerSetup implements ServerSetupTask {
        private final ModelNode address = Operations.createAddress("system-property", KEY);

        @Override
        public void setup(final ManagementClient managementClient, final String containerId) throws Exception {
            final ModelNode op = Operations.createAddOperation(address);
            op.get("value").set(VALUE);
            executeForSuccess(managementClient, op);
        }

        @Override
        public void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
            executeForSuccess(managementClient, Operations.createRemoveOperation(address));
        }
    }

    @ArquillianResource
    private Deployer deployer;
    @ArquillianResource
    private ManagementClient client;

    @Deployment(managed = false, name = "test-deployment")
    public static JavaArchive create() {
        final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "unmanaged-deployment-test.jar")
                .addClass(SystemPropertyServiceActivator.class)
                .addAsServiceProvider(ServiceActivator.class, SystemPropertyServiceActivator.class);
        return archive;
    }

    @Test
    @Order(1)
    public void undeployFirst() throws Exception {
        // Undeploy first which should not really do anything
        deployer.undeploy("test-deployment");
        // Now deploy the deployment which should execute the setup
        deployer.deploy("test-deployment");
        testSystemProperty(KEY, VALUE);

    }

    @Test
    @Order(2)
    public void contentDeployedThenUndeployed() throws Exception {
        // After the undeploy, the tearDown() won't happen until the class is complete
        deployer.undeploy("test-deployment");
        testSystemProperty(KEY, VALUE);
    }

    @Override
    ManagementClient getClient() {
        return client;
    }
}
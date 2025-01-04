/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class ServerSetupAfterClassTestCase extends TestOperations {
    @ArquillianResource
    private ManagementClient client;

    @Deployment(name = "ServerSetupAfterClassTestCase.jar")
    public static JavaArchive deployment() {
        // Create a dummy deployment so the client can be injected
        return ShrinkWrap.create(JavaArchive.class, "ServerSetupAfterClassTestCase.jar").addManifest();
    }

    @Test
    public void testSystemPropertyRemoved() throws Exception {
        // All deployments from the ServerSetupDeploymentTestCase should have been undeployed and the
        // ServerSetupTask.tearDown() should have been invoked
        testSystemProperty(ServerSetupTestSuite.SYSTEM_PROPERTY_KEY);
    }

    /**
     * Tests system state established by {@link ServerSetupAssumptionViolationTestCase}.
     */
    @Test
    public void testAssumptionViolated() {
        // BeforeSetup's tearDown should have executed
        testInVMProperty(ServerSetupAssumptionViolationTestCase.BeforeSetup.PROPERTY, "tearDown");
        // AssumptionViolatedSetup's tearDown should not have executed
        // Note: A ServerSetupTask should not change state before throwing AVE, but we do here so we can verify setup ran
        testInVMProperty(ServerSetupAssumptionViolationTestCase.AssumptionViolatedSetup.PROPERTY, "setup");
        // AfterSetup should not have run at all, as AssumptionViolatedSetup threw an AVE
        testInVMProperty(ServerSetupAssumptionViolationTestCase.AfterSetup.PROPERTY, "");
    }

    /**
     * Tests system state established by {@link ServerSetupUnmanagedAssumptionViolationTestCase}.
     */
    @Test
    public void testUnmanagedAssumptionViolated() {
        // BeforeSetup's tearDown should have executed
        testInVMProperty(ServerSetupUnmanagedAssumptionViolationTestCase.BeforeSetup.PROPERTY, "tearDown");
        // AssumptionViolatedSetup's tearDown should not have executed
        // Note: A ServerSetupTask should not change state before throwing AVE, but we do here so we can verify setup ran
        testInVMProperty(ServerSetupUnmanagedAssumptionViolationTestCase.AssumptionViolatedSetup.PROPERTY, "setup");
        // AfterSetup should not have run at all, as AssumptionViolatedSetup threw an AVE
        testInVMProperty(ServerSetupUnmanagedAssumptionViolationTestCase.AfterSetup.PROPERTY, "");
    }

    private void testInVMProperty(String property, String expected) {
        String value = System.getProperty(property, "");
        System.clearProperty(property); // housekeeping
        assertEquals(expected, value, "Unexpected value for " + property);
    }

    @Override
    ManagementClient getClient() {
        return client;
    }
}

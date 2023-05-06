/*
 * Copyright 2017 Red Hat, Inc.
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

package org.jboss.as.arquillian.container.managed;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Arquillian.class)
@ServerSetup(ServerSetupDeploymentTestCase.SystemPropertySetupTask.class)
@RunAsClient
public class ServerSetupDeploymentTestCase extends TestOperations {

    static class SystemPropertySetupTask implements ServerSetupTask {

        private final ModelNode address = Operations.createAddress("system-property", ServerSetupTestSuite.SYSTEM_PROPERTY_KEY);

        @Override
        public void setup(final ManagementClient managementClient, final String containerId) throws Exception {
            // Add a system property
            final ModelNode op = Operations.createAddOperation(address);
            op.get("value").set(ServerSetupTestSuite.SYSTEM_PROPERTY_VALUE);
            final ModelNode result = managementClient.getControllerClient().execute(op);
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new RuntimeException(
                        "Failed to add system property: " + Operations.getFailureDescription(result).asString());
            }
        }

        @Override
        public void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
            // Remove the system property
            final ModelNode op = Operations.createRemoveOperation(address);
            final ModelNode result = managementClient.getControllerClient().execute(op);
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new RuntimeException(
                        "Failed to remove system property: " + Operations.getFailureDescription(result).asString());
            }
        }
    }

    private static final String DEPLOYMENT1 = "test-unmanaged-deployment-1.war";
    private static final String DEPLOYMENT2 = "test-unmanaged-deployment-2.war";

    @ArquillianResource
    private Deployer deployer;

    @ArquillianResource
    protected ManagementClient client;

    @Deployment(name = DEPLOYMENT1)
    public static WebArchive createDeployment1() throws Exception {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT1)
                .addClass(HelloWorldServlet.class);
    }

    @Deployment(managed = false, name = DEPLOYMENT2)
    public static WebArchive createDeployment2() throws Exception {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT2)
                .addClass(HelloWorldServlet.class);
    }

    @Test
    public void testManualUndeploy() throws IOException {
        // Undeploy the unmanaged deployment even though it's not deployed, this tests whether or not on the AfterClass
        // listener the tear down will happen.
        deployer.undeploy(DEPLOYMENT2);
        // THe system property should still be there because the first deployment is still there
        testSystemProperty(ServerSetupTestSuite.SYSTEM_PROPERTY_KEY, ServerSetupTestSuite.SYSTEM_PROPERTY_VALUE);
        // Test leaving an unmanaged deployment deployed to ensure it will be undeployed
        deployer.deploy(DEPLOYMENT2);
    }

    @Override
    ManagementClient getClient() {
        return client;
    }
}

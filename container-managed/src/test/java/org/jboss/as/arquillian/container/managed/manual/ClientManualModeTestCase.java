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

package org.jboss.as.arquillian.container.managed.manual;

import java.io.IOException;

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ArchiveDeployer;
import org.jboss.as.arquillian.container.ManagementClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that the injected management client and archive deployer are usable on the appropriate container.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ClientManualModeTestCase extends AbstractManualModeTestCase {

    @ArquillianResource
    @TargetsContainer(PRIMARY_CONTAINER)
    private ArchiveDeployer primaryDeployer;

    @ArquillianResource
    @TargetsContainer(SECONDARY_CONTAINER)
    private ArchiveDeployer secondaryDeployer;

    @Test
    public void testDeploy() throws Exception {
        testDeploy(primaryDeployer, primaryClient, PRIMARY_CONTAINER);
        testDeploy(secondaryDeployer, secondaryClient, SECONDARY_CONTAINER);
    }

    private static void testDeploy(final ArchiveDeployer deployer, final ManagementClient client, final String containerName) throws IOException, DeploymentException {
        if (!controller.isStarted(containerName)) {
            controller.start(containerName);
        }
        final int currentDeployments = getCurrentDeploymentCount(client);
        // Deploy both deployments
        try {
            deployer.deploy(createDeployment());
            // Read each result, we should have two results for the first op and one for the second
            final int newDeployments = getCurrentDeploymentCount(client);
            Assert.assertTrue("Expected 1 deployments found " + (newDeployments - currentDeployments) + " for container " + containerName,
                    newDeployments == (1 + currentDeployments));
        } finally {
            deployer.undeploy("dep1");
            controller.stop(containerName);
        }
    }
}

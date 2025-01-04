/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ArchiveDeployer;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test deploying through an injected {@link ArchiveDeployer}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class ClientDeploymentTestCase {

    @ArquillianResource
    private ArchiveDeployer deployer;

    @Deployment
    public static WebArchive create() {
        return createDeployment(null);
    }

    @Test
    public void globalDeploymentTest(@ArquillianResource ManagementClient client) throws Exception {
        Assertions.assertNotNull(deployer, "Expected the ArchiveDeployer to be injected");
        testDeploy(deployer, client);
    }

    @Test
    public void parameterDeploymentTest(@ArquillianResource ManagementClient client,
            @ArquillianResource ArchiveDeployer deployer) throws Exception {
        Assertions.assertNotNull(deployer, "Expected the ArchiveDeployer to be injected");
        testDeploy(deployer, client);
    }

    private void testDeploy(final ArchiveDeployer deployer, final ManagementClient client) throws Exception {
        final WebArchive deployment = createDeployment("test-client-deployment.war");
        Assertions.assertEquals(deployment.getName(), deployer.deploy(deployment));

        // Check the server for the deployment
        final ModelNode address = Operations.createAddress("deployment", deployment.getName());
        final ModelNode result = client.getControllerClient()
                .execute(Operations.createReadAttributeOperation(address, "status"));
        if (!Operations.isSuccessfulOutcome(result)) {
            Assertions.fail(Operations.getFailureDescription(result).asString());
        }
        Assertions.assertEquals("OK", Operations.readResult(result).asString());
        deployer.undeploy(deployment.getName());
    }

    private static WebArchive createDeployment(final String name) {
        if (name == null) {
            return ShrinkWrap.create(WebArchive.class)
                    .addClass(HelloWorldServlet.class);
        }
        return ShrinkWrap.create(WebArchive.class, name)
                .addClass(HelloWorldServlet.class);
    }
}

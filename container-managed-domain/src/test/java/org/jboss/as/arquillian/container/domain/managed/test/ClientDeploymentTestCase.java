/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.domain.managed.test;

import java.util.Collections;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.domain.ArchiveDeployer;
import org.jboss.as.arquillian.container.domain.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.arquillian.domain.api.TargetsServerGroup;

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
    @TargetsServerGroup("main-server-group")
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
        Assertions.assertEquals(deployment.getName(), deployer.deploy(deployment, Collections.singleton("main-server-group")));

        // Check the server for the deployment
        final ModelNode address = Operations.createAddress("deployment", deployment.getName());
        ModelNode result = client.getControllerClient().execute(Operations.createReadAttributeOperation(address, "name"));
        if (!Operations.isSuccessfulOutcome(result)) {
            Assertions.fail(Operations.getFailureDescription(result).asString());
        }
        Assertions.assertEquals(deployment.getName(), Operations.readResult(result).asString());

        final ModelNode serverGroupAddress = Operations.createAddress("server-group", "main-server-group", "deployment",
                deployment.getName());
        result = client.getControllerClient().execute(Operations.createReadAttributeOperation(serverGroupAddress, "enabled"));
        if (!Operations.isSuccessfulOutcome(result)) {
            Assertions.fail(Operations.getFailureDescription(result).asString());
        }
        Assertions.assertTrue(Operations.readResult(result).asBoolean(), "The deployment was expected to be enabled");
        deployer.undeploy(deployment.getName(), "main-server-group");
    }

    private static WebArchive createDeployment(final String name) {
        if (name == null) {
            return ShrinkWrap.create(WebArchive.class)
                    .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        }
        return ShrinkWrap.create(WebArchive.class, name)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}
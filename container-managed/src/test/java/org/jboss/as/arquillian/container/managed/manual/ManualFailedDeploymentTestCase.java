/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed.manual;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.wildfly.plugin.tools.DeploymentManager;
import org.wildfly.plugin.tools.server.ServerManager;

/**
 * Tests manual deployment control with expected deployment failures.
 * Uses {@code @Deployment(managed = false)} to have full control over when deployments happen.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ArquillianTest
@RunAsClient
@Tag("ManualMode")
public class ManualFailedDeploymentTestCase {
    private static final String CONTAINER_NAME = "jboss";
    private static final String DEPLOYMENT_NAME = "manual-failed-deployment";

    @ArquillianResource
    private ContainerController controller;

    @ArquillianResource
    private Deployer deployer;

    @Deployment(managed = false, name = DEPLOYMENT_NAME)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME + ".war")
                .addAsWebInfResource(new StringAsset("<web-app><invalid/>"), "web.xml");
    }

    @BeforeEach
    public void start() {
        controller.start(CONTAINER_NAME);
    }

    @AfterEach
    public void stop() {
        if (controller.isStarted(CONTAINER_NAME)) {
            controller.stop(CONTAINER_NAME);
        }
    }

    @Test
    public void testManualDeploymentFailure(@ArquillianResource ServerManager serverManager) throws Exception {
        controller.start(CONTAINER_NAME);
        final DeploymentManager deploymentManager = serverManager.deploymentManager();
        final String warName = DEPLOYMENT_NAME + ".war";

        // Attempt to deploy - this should fail but be handled by the pattern
        deployer.deploy(DEPLOYMENT_NAME);

        // Verify the deployment is NOT on the server
        Assertions.assertFalse(deploymentManager.hasDeployment(warName), () -> {
            try {
                return String.format("Deployment %s should not exist but was found in %s",
                        warName, deploymentManager.getDeploymentNames());
            } catch (IOException e) {
                return "Deployment " + warName + " should not exist";
            }
        });

        // Cleanup - undeploy should handle non-existent deployment gracefully
        deployer.undeploy(DEPLOYMENT_NAME);
    }
}

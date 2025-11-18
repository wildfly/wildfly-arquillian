/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed;

import java.io.IOException;
import java.util.Set;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.plugin.tools.DeploymentManager;
import org.wildfly.plugin.tools.server.ServerManager;

/**
 * Tests that the deployment fails and is not deployed.
 *
 * @author <a href="mailto:jperkins@ibm.com">James R. Perkins</a>
 */
@ArquillianTest
public class FailedDeploymentTestCase {

    @ArquillianResource
    private ServerManager serverManager;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, FailedDeploymentTestCase.class.getSimpleName() + ".war")
                .addAsWebInfResource(new StringAsset("<web-app><invalid/>"), "web.xml");
    }

    @Test
    public void testNoDeployments() throws Exception {
        final DeploymentManager deploymentManager = serverManager.deploymentManager();
        Assertions.assertFalse(deploymentManager.hasDeployment(FailedDeploymentTestCase.class.getSimpleName() + ".war"), () -> {
            Set<String> deployments = Set.of();
            try {
                deployments = deploymentManager.getDeploymentNames();
            } catch (IOException ignore) {
            }
            return String.format("Deployment %s.war found in %s", FailedDeploymentTestCase.class.getSimpleName(), deployments);
        });
    }
}

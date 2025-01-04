/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.domain;

import java.io.IOException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.domain.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.arquillian.domain.api.TargetsServerGroup;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
public class ElytronIntegrationTestCase {

    @Deployment
    @TargetsServerGroup("main-server-group")
    public static JavaArchive deployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addAsResource(new StringAsset("Dependencies: org.jboss.dmr, org.jboss.as.controller\n"),
                        "META-INF/MANIFEST.MF");
    }

    @Test
    @RunAsClient
    public void testClientUser(@ArquillianResource ManagementClient client) throws IOException {
        final ModelNode result = client.getControllerClient().execute(Operations.createOperation("whoami"));
        Assertions.assertTrue(Operations.isSuccessfulOutcome(result));
        final ModelNode identity = Operations.readResult(result);
        Assertions.assertEquals("test-admin",
                identity.get("identity", "username").asString(),
                "Expected the connected user to be test-admin");
    }

    @Test
    @TargetsServerGroup("main-server-group")
    @Disabled("Currently domain does not allow injecting clients into in container tests")
    public void testInContainerClientUser(@ArquillianResource ManagementClient client) throws IOException {
        final ModelNode result = client.getControllerClient().execute(Operations.createOperation("whoami"));
        Assertions.assertTrue(Operations.isSuccessfulOutcome(result));
        final ModelNode identity = Operations.readResult(result);
        Assertions.assertEquals("test-admin",
                identity.get("identity", "username").asString(),
                "Expected the connected user to be test-admin");
    }
}

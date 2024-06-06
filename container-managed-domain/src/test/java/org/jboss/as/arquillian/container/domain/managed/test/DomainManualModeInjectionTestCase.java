/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.domain.managed.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.domain.Domain;
import org.jboss.as.arquillian.container.domain.ManagementClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.arquillian.domain.api.DomainContainerController;
import org.wildfly.arquillian.domain.api.TargetsServerGroup;

/**
 * For Domain server DeployableContainer implementations, the DeployableContainer will register
 * all groups/individual servers it controls as Containers in Arquillian's Registry during start.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
@RunWith(Arquillian.class)
@Category(ManualMode.class)
public class DomainManualModeInjectionTestCase {
    static final String PRIMARY_CONTAINER = "jboss";
    static final String SECONDARY_CONTAINER = "wildfly";

    @ArquillianResource
    private DomainContainerController controller;

    @ArquillianResource
    @TargetsContainer(SECONDARY_CONTAINER)
    private ManagementClient secondaryClient;

    @ArquillianResource
    @TargetsContainer(PRIMARY_CONTAINER)
    private ManagementClient primaryClient;

    @Deployment(managed = false)
    @TargetsServerGroup("main-server-group")
    public static WebArchive create1() {
        return ShrinkWrap.create(WebArchive.class)
                // Required for JUnit when running in ARQ
                .addClass(ManualMode.class);
    }

    @After
    public void stopAllContainers() throws Exception {
        if (controller.isStarted(PRIMARY_CONTAINER)) {
            controller.stop(PRIMARY_CONTAINER);
        }
        if (controller.isStarted(SECONDARY_CONTAINER)) {
            controller.stop(SECONDARY_CONTAINER);
        }
    }

    @Test
    @TargetsContainer("main:server-two")
    public void testPrimaryClientInContainer() throws Exception {
        controller.start(PRIMARY_CONTAINER);
        testClient(primaryClient);
    }

    @Test
    @TargetsContainer("main:server-two")
    public void testSecondaryClientInContainer() throws Exception {
        controller.start(SECONDARY_CONTAINER);
        testClient(secondaryClient);
    }

    @Test
    @RunAsClient
    public void testPrimaryClientAsClient() throws Exception {
        controller.start(PRIMARY_CONTAINER);
        testClient(primaryClient);
    }

    @Test
    @RunAsClient
    public void testSecondaryClientAsClient() throws Exception {
        controller.start(SECONDARY_CONTAINER);
        testClient(secondaryClient);
    }

    private static void testClient(final ManagementClient client) {
        Assert.assertTrue("The primary container should have been started", client.isDomainInRunningState());
        Assert.assertTrue(
                client.isServerStarted(new Domain.Server("server-one", client.getLocalHostName(), "main-server-group", true)));
    }
}

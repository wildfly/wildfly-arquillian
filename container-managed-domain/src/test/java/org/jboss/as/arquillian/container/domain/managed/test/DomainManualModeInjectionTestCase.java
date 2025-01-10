/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.domain.managed.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.domain.Domain;
import org.jboss.as.arquillian.container.domain.ManagementClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.arquillian.domain.api.DomainContainerController;
import org.wildfly.arquillian.domain.api.TargetsServerGroup;

/**
 * For Domain server DeployableContainer implementations, the DeployableContainer will register
 * all groups/individual servers it controls as Containers in Arquillian's Registry during start.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
@ExtendWith(ArquillianExtension.class)
@Tag("ManualMode")
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

    @AfterEach
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
        Assertions.assertTrue(client.isDomainInRunningState(), "The primary container should have been started");
        Assertions.assertTrue(
                client.isServerStarted(new Domain.Server("server-one", client.getLocalHostName(), "main-server-group", true)));
    }
}

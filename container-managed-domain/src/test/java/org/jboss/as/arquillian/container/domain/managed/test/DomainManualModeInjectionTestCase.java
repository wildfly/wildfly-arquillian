/*
 * Copyright 2016 Red Hat, Inc.
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

package org.jboss.as.arquillian.container.domain.managed.test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.domain.Domain;
import org.jboss.as.arquillian.container.domain.ManagementClient;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.client.helpers.Operations.CompositeOperationBuilder;
import org.jboss.dmr.ModelNode;
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
        Assert.assertTrue(client.isServerStarted(new Domain.Server("server-one", "master", "main-server-group", true)));
    }
}

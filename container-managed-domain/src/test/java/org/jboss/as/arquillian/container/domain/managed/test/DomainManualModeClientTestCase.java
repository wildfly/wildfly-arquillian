/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.domain.managed.test;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.domain.ManagementClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * For Domain server DeployableContainer implementations, the DeployableContainer will register
 * all groups/individual servers it controls as Containers in Arquillian's Registry during start.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
@Category(ManualMode.class)
public class DomainManualModeClientTestCase extends AbstractDomainManualModeTestCase {
    private static final String CONTAINER_NAME = "jboss";

    @ArquillianResource
    @TargetsContainer(CONTAINER_NAME)
    private ManagementClient client;

    @ArquillianResource
    private Deployer deployer;

    @AfterClass
    public static void stop() throws Exception {
        if (controller.isStarted(CONTAINER_NAME)) {
            controller.stop(CONTAINER_NAME);
        }
    }

    @Before
    public void startOnce() throws Exception {
        if (!controller.isStarted(CONTAINER_NAME)) {
            controller.start(CONTAINER_NAME);
        }
    }

    @Override
    protected String containerName() {
        return CONTAINER_NAME;
    }

    @Override
    protected ManagementClient client() {
        return client;
    }

    @Override
    protected Deployer deployer() {
        return deployer;
    }
}

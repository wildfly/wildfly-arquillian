/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.domain.managed.test;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.domain.ManagementClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * For Domain server DeployableContainer implementations, the DeployableContainer will register
 * all groups/individual servers it controls as Containers in Arquillian's Registry during start.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
@Tag("ManualMode")
public class DomainManualModeClientTestCase extends AbstractDomainManualModeTestCase {
    private static final String CONTAINER_NAME = "jboss";

    @ArquillianResource
    @TargetsContainer(CONTAINER_NAME)
    private ManagementClient client;

    @ArquillianResource
    private Deployer deployer;

    @AfterEach
    public void stop() {
        if (controller.isStarted(CONTAINER_NAME)) {
            controller.stop(CONTAINER_NAME);
        }
    }

    @BeforeEach
    public void startOnce() {
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

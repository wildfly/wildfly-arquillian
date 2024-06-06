/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.domain.managed.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
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
public class ManagedDomainControlTestCase {
    private static final String CONTAINER_NAME = "jboss";

    @Deployment
    @TargetsServerGroup("main-server-group")
    public static WebArchive create1() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @ArquillianResource
    private DomainContainerController controller;

    @Test
    @InSequence(1)
    public void shouldNotBeAbleToStopContainerInContainer() throws Exception {
        // Should fail as this should be read-only
        try {
            controller.stopServer(CONTAINER_NAME, "master", "server-two");
            Assert.fail("Expected stop for server-two to fail as this should be a read-only DomainSupport");
        } catch (RuntimeException ignore) {
        }
    }

    @Test
    @InSequence(2)
    public void shouldNotBeAbleToStartContainerInContainer() throws Exception {
        // Should fail as this should be read-only
        try {
            controller.startServer(CONTAINER_NAME, "master", "server-two");
            Assert.fail("Expected start for server-two to fail as this should be a read-only DomainSupport");
        } catch (RuntimeException ignore) {
        }
    }

    @Test
    @InSequence(3)
    @RunAsClient
    public void shouldNotBeAbleToStopContainerAsClient() throws Exception {
        // Should fail as this should be read-only
        try {
            controller.stopServer(CONTAINER_NAME, "master", "server-two");
            Assert.fail("Expected stop for server-two to fail as this should be a read-only DomainSupport");
        } catch (RuntimeException ignore) {
        }
    }

    @Test
    @InSequence(4)
    @RunAsClient
    public void shouldNotBeAbleToStartContainerAsClient() throws Exception {
        // Should fail as this should be read-only
        try {
            controller.startServer(CONTAINER_NAME, "master", "server-two");
            Assert.fail("Expected start for server-two to fail as this should be a read-only DomainSupport");
        } catch (RuntimeException ignore) {
        }
    }
}

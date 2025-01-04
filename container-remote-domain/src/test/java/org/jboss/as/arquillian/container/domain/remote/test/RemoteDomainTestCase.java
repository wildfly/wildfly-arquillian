/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.domain.remote.test;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * For Domain server DeployableContianer implementations, the DeployableContainer will register
 * all groups/individual servers it controls as Containers in Arquillians Registry during start.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
@ExtendWith(ArquillianExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RemoteDomainTestCase {

    @ArquillianResource
    ContainerController controller;

    @Deployment(name = "dep1")
    @TargetsContainer("main-server-group")
    public static JavaArchive create1() {
        return ShrinkWrap.create(JavaArchive.class);
    }

    @Test
    @Order(1)
    @OperateOnDeployment("dep1")
    @TargetsContainer("master:server-one")
    public void shouldRunInContainer1() throws Exception {
        Assertions.assertTrue(controller.isStarted("master:server-one"));
        System.out.println("in..container");
    }

    @Test
    @Order(2)
    @OperateOnDeployment("dep1")
    @TargetsContainer("master:server-two")
    public void shouldRunInContainer2() throws Exception {
        Assertions.assertTrue(controller.isStarted("master:server-two"));
    }
}

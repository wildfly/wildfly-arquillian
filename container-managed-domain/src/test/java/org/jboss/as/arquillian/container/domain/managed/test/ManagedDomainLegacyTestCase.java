/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.domain.managed.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * For Domain server DeployableContainer implementations, the DeployableContainer will register
 * all groups/individual servers it controls as Containers in Arquillian's Registry during start.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
@RunWith(Arquillian.class)
public class ManagedDomainLegacyTestCase {

    @Deployment(name = "dep1")
    @TargetsContainer("main-server-group")
    public static WebArchive create1() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @Test
    @OperateOnDeployment("dep1")
    @TargetsContainer("master:server-one")
    public void shouldRunInContainer1() throws Exception {
        // Get the logger path which should contain the name of the server
        final String logDir = System.getProperty("jboss.server.log.dir");
        Assert.assertNotNull("Could not determine the jboss.server.log.dir property", logDir);
        Assert.assertTrue("Log dir should contain server-one: " + logDir, logDir.contains("server-one"));
    }

    @Test
    @TargetsContainer("master:server-two")
    public void shouldRunInContainer2() throws Exception {
        // Get the logger path which should contain the name of the server
        final String logDir = System.getProperty("jboss.server.log.dir");
        Assert.assertNotNull("Could not determine the jboss.server.log.dir property", logDir);
        Assert.assertTrue("Log dir should contain server-two: " + logDir, logDir.contains("server-two"));
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * AS7-1415 Ensures injection of the {@link ManagementClient} is working correctly
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
@RunWith(Arquillian.class)
public class InjectManagementClientTestCase {

    @ArquillianResource
    private ManagementClient managementClient;

    @Deployment(testable = false)
    public static WebArchive createDeployment() throws Exception {
        return ShrinkWrap.create(WebArchive.class).addClass(HelloWorldServlet.class);
    }

    @Test
    public void ensureManagementClientInjected() {
        Assert.assertNotNull("Management client must be injected", managementClient);
        Assert.assertTrue("Management client should report server as running",
                managementClient.isServerInRunningState());
    }
}

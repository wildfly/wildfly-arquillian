/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * A client-side test case that verifies that the root deployment URL can be injected
 * into the test case.
 *
 * @author <a href="http://community.jboss.org/people/dan.j.allen">Dan Allen</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ManagedAsClientEnterpriseArchiveServletTestCase {

    private static final String CONTEXT_NAME = "test-hello-world";

    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() throws Exception {
        WebArchive war = ShrinkWrap.create(WebArchive.class, CONTEXT_NAME + ".war").addClass(HelloWorldServlet.class);
        return ShrinkWrap.create(EnterpriseArchive.class).addAsModule(war);
    }

    @ArquillianResource
    private URL deploymentUrl;

    @Test
    public void shouldBeAbleToInvokeServlet() throws Exception {
        Assert.assertNotNull(deploymentUrl);
        String result = getContent(new URL(deploymentUrl.toString() + HelloWorldServlet.URL_PATTERN.substring(1)));
        Assert.assertEquals(HelloWorldServlet.GREETING, result);
    }

    @Test
    public void testWebContext() {
        Assert.assertTrue(
                String.format("Expected context to start with /%s, but found %s", CONTEXT_NAME, deploymentUrl.getPath()),
                deploymentUrl.getPath().startsWith("/" + CONTEXT_NAME));
    }

    private String getContent(URL url) throws Exception {
        InputStream is = url.openStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int read;
            while ((read = is.read()) != -1) {
                out.write(read);
            }
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
        return out.toString();
    }

}

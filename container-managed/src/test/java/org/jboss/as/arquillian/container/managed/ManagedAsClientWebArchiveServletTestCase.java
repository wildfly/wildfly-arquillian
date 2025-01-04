/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * A client-side test case that verifies that the root deployment URL can be injected
 * into the test case.
 *
 * @author <a href="http://community.jboss.org/people/dan.j.allen">Dan Allen</a>
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class ManagedAsClientWebArchiveServletTestCase {

    private static final String CONTEXT_NAME = "foo-v1.2";

    @Deployment(testable = false)
    @OverProtocol("Servlet 5.0")
    public static WebArchive createDeployment() throws Exception {
        return ShrinkWrap.create(WebArchive.class, CONTEXT_NAME + ".war").addClass(HelloWorldServlet.class);
    }

    @ArquillianResource
    URL deploymentUrl;

    @Test
    public void shouldBeAbleToInvokeServlet() throws Exception {
        Assertions.assertNotNull(deploymentUrl);
        Assertions.assertTrue(deploymentUrl.toString().endsWith(CONTEXT_NAME + "/"),
                "deploymentUrl should end with " + CONTEXT_NAME + "/, but is " + deploymentUrl +
                        ", the context URL seems to be wrong");
        String result = getContent(new URL(deploymentUrl.toString() + HelloWorldServlet.URL_PATTERN.substring(1)));
        Assertions.assertEquals(HelloWorldServlet.GREETING, result);
    }

    @Test
    public void testWebContext() {
        Assertions.assertTrue(
                deploymentUrl.getPath().startsWith("/" + CONTEXT_NAME),
                String.format("Expected context to start with /%s but found %s", CONTEXT_NAME, deploymentUrl.getPath()));
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

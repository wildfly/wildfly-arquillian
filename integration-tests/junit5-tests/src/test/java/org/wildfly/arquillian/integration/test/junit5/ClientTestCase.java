/*
 * Copyright 2021 Red Hat, Inc.
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

package org.wildfly.arquillian.integration.test.junit5;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.client.helpers.Operations.CompositeOperationBuilder;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
@ServerSetup(ClientTestCase.SystemPropertyServerSetupTask.class)
@RunAsClient
public class ClientTestCase {

    private static final Map<String, String> PROPERTIES = new HashMap<>();

    static {
        PROPERTIES.put("prop1", "servlet-value1");
        PROPERTIES.put("prop2", "servlet-value2");
        PROPERTIES.put("prop3", "servlet-value3");
    }

    @ArquillianResource
    private ManagementClient client;
    @ArquillianResource
    private URL url;

    @Deployment
    public static WebArchive create() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(GreeterServlet.class);
    }

    @Test
    public void testGreet() throws Exception {
        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder(URI.create(url + GreeterServlet.URL_PATTERN))
                .build();
        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        final String body = response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(GreeterServlet.GREETING, body);
    }

    @Test
    public void testSystemProperties() throws Exception {
        for (Map.Entry<String, String> entry : PROPERTIES.entrySet()) {
            Assertions.assertEquals(entry.getValue(), getProperty(entry.getKey()));
        }
    }

    private String getProperty(final String key) throws IOException {
        final ModelNode address = Operations.createAddress("system-property", key);
        final ModelNode result = client.getControllerClient()
                .execute(Operations.createReadAttributeOperation(address, "value"));
        Assertions.assertTrue(Operations.isSuccessfulOutcome(result),
                () -> Operations.getFailureDescription(result).asString());
        return Operations.readResult(result).asString();
    }

    public static class SystemPropertyServerSetupTask implements ServerSetupTask {

        @Override
        public void setup(final ManagementClient managementClient, final String containerId) throws Exception {
            final CompositeOperationBuilder builder = CompositeOperationBuilder.create();
            for (Map.Entry<String, String> entry : PROPERTIES.entrySet()) {
                final ModelNode op = Operations.createAddOperation(Operations.createAddress("system-property", entry.getKey()));
                op.get(ClientConstants.VALUE).set(entry.getValue());
                builder.addStep(op);
            }

            final ModelNode result = managementClient.getControllerClient().execute(builder.build());
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new RuntimeException(
                        "Failed to configure properties: " + Operations.getFailureDescription(result).asString());
            }
        }

        @Override
        public void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
            final CompositeOperationBuilder builder = CompositeOperationBuilder.create();
            for (String key : PROPERTIES.keySet()) {
                builder.addStep(Operations.createRemoveOperation(Operations.createAddress("system-property", key)));
            }

            final ModelNode result = managementClient.getControllerClient().execute(builder.build());
            if (!Operations.isSuccessfulOutcome(result)) {
                throw new RuntimeException(
                        "Failed to configure properties: " + Operations.getFailureDescription(result).asString());
            }
        }
    }
}

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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.client.helpers.Operations.CompositeOperationBuilder;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
@ServerSetup(InContainerTestCase.SystemPropertyServerSetupTask.class)
public class InContainerTestCase implements InContainerTestAssertion {

    private static final Map<String, String> PROPERTIES = new HashMap<>();

    static {
        PROPERTIES.put("prop1", "value1");
        PROPERTIES.put("prop2", "value2");
        PROPERTIES.put("prop3", "value3");
    }

    @Inject
    private Greeter greeter;

    @Deployment
    public static JavaArchive create() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(Greeter.class, InContainerTestAssertion.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testInjection() {
        Assertions.assertNotNull(greeter);
        Assertions.assertEquals("Hello Tester!", greeter.greet("Tester"));
    }

    @Test
    @OverProtocol("Servlet 5.0")
    public void testServletProtocol() {
        Assertions.assertNotNull(greeter);
        Assertions.assertEquals("Hello Servlet!", greeter.greet("Servlet"));
    }

    @Test
    public void testSystemProperties() {
        for (Map.Entry<String, String> entry : PROPERTIES.entrySet()) {
            Assertions.assertEquals(entry.getValue(), getProperty(entry.getKey()));
        }
    }

    private static String getProperty(final String key) {
        if (System.getSecurityManager() == null) {
            return System.getProperty(key);
        }
        return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key));
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

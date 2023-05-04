/*
 * Copyright 2020 Red Hat, Inc.
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

package org.jboss.as.arquillian.container.managed.manual;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Category(ManualMode.class)
@RunWith(Arquillian.class)
@RunAsClient
public class YamlConfigTestCase {
    private static final String CONTAINER_ID = "yaml";

    @ArquillianResource
    @SuppressWarnings({"unused", "StaticVariableMayNotBeInitialized"})
    private static ContainerController controller;

    @ArquillianResource
    @SuppressWarnings({"unused", "InstanceVariableMayNotBeInitialized"})
    @TargetsContainer(CONTAINER_ID)
    private ManagementClient defaultClient;

    @Deployment(managed = false, name = "dep1")
    @TargetsContainer(CONTAINER_ID)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                // Required for JUnit when running in ARQ
                .addClass(ManualMode.class);
    }

    @After
    public void shutdown() {
        if (controller.isStarted(CONTAINER_ID)) {
            controller.stop(CONTAINER_ID);
        }
    }

    @Test
    public void singleYamlFile() throws Exception {
        controller.start(CONTAINER_ID, Map.of("yamlConfiguration", resolveYamlFile("test-config-1.yml")));

        // Check the system property
        ModelNode address = Operations.createAddress("system-property", "test-yaml1");
        ModelNode op = Operations.createReadAttributeOperation(address, "value");
        Assert.assertEquals("yaml1", executeOperation(defaultClient, op).asString());

        // Stop the container and restart it, the system property should exist
        controller.stop(CONTAINER_ID);
        controller.start(CONTAINER_ID, Map.of("yamlConfiguration", resolveYamlFile("test-config-2.yml")));
        // Reading the first resource should fail
        executeOperation(defaultClient, op, true);

        address = Operations.createAddress("system-property", "test-yaml2");
        op = Operations.createReadAttributeOperation(address, "value");
        Assert.assertEquals("yaml2", executeOperation(defaultClient, op).asString());
    }

    @Test
    public void multipleCommaYamlFile() throws Exception {
        multipleYamlFile(",");
    }

    @Test
    public void multipleSpaceYamlFile() throws Exception {
        multipleYamlFile(" ");
    }

    private void multipleYamlFile(final String delimiter) throws Exception {
        controller.start(CONTAINER_ID, Map.of("yamlConfiguration", resolveYamlFiles(delimiter)));

        // Check the system properties
        ModelNode address = Operations.createAddress("system-property", "test-yaml1");
        ModelNode op = Operations.createReadAttributeOperation(address, "value");
        Assert.assertEquals("yaml1", executeOperation(defaultClient, op).asString());

        address = Operations.createAddress("system-property", "test-yaml2");
        op = Operations.createReadAttributeOperation(address, "value");
        Assert.assertEquals("yaml2", executeOperation(defaultClient, op).asString());
    }

    private static ModelNode executeOperation(final ManagementClient client, final ModelNode op) throws IOException {
        return executeOperation(client, op, false);
    }

    private static ModelNode executeOperation(final ManagementClient client, final ModelNode op, final boolean expectFailure) throws IOException {
        final ModelNode result = client.getControllerClient().execute(op);
        if (expectFailure) {
            Assert.assertFalse(String.format("Expected operation %s to fail: %n%s", op, result), Operations.isSuccessfulOutcome(result));
            return result;
        }
        if (!Operations.isSuccessfulOutcome(result)) {
            Assert.fail(Operations.getFailureDescription(result).asString());
        }
        return Operations.readResult(result);
    }

    private static String resolveYamlFile(final String name) throws URISyntaxException {
        final URL resource = YamlConfigTestCase.class.getResource("/" + name);
        Assert.assertNotNull("Could not find " + name, resource);
        return Path.of(resource.toURI()).toString();
    }

    private static String resolveYamlFiles(final String delimiter) throws URISyntaxException {
        return resolveYamlFile("test-config-1.yml") + delimiter + resolveYamlFile("test-config-2.yml");
    }
}

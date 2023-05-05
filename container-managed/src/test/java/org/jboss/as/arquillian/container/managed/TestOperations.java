/*
 * Copyright 2017 Red Hat, Inc.
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

package org.jboss.as.arquillian.container.managed;

import java.io.IOException;
import java.util.List;

import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.junit.Assert;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
abstract class TestOperations {

    abstract ManagementClient getClient();

    void testSystemProperty(final String key) throws IOException {
        final ModelNode op = Operations.createOperation("read-children-names");
        op.get("child-type").set("system-property");
        final List<ModelNode> result = executeForSuccess(getClient(), op).asList();
        for (ModelNode property : result) {
            Assert.assertNotEquals(String.format("The key '%s' should have been removed from the server", key), key,
                    property.asString());
        }
    }

    void testSystemProperty(final String key, final String value) throws IOException {
        final ModelNode address = Operations.createAddress("system-property", key);
        final ModelNode result = executeForSuccess(getClient(), Operations.createReadResourceOperation(address));
        Assert.assertEquals(value, result.get("value").asString());
    }

    static ModelNode executeForSuccess(final ManagementClient client, final ModelNode op) throws IOException {
        final ModelNode result = client.getControllerClient().execute(op);
        if (!Operations.isSuccessfulOutcome(result)) {
            throw new RuntimeException(String.format("Failed to executeForSuccess operation :%s%n%s", op,
                    Operations.getFailureDescription(result).asString()));
        }
        return Operations.readResult(result);
    }
}

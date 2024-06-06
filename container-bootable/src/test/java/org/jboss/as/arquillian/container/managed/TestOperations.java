/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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

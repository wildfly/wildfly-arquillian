/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.arquillian.integration.test.junit5.server.setup;

import java.util.Map;

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class SystemPropertyServerSetupTask implements ServerSetupTask {
    public final Map<String, String> properties;

    public SystemPropertyServerSetupTask(final Map<String, String> properties) {
        this.properties = Map.copyOf(properties);
    }

    @Override
    public void setup(final ManagementClient managementClient, final String containerId) throws Exception {
        final Operations.CompositeOperationBuilder builder = Operations.CompositeOperationBuilder.create();
        for (var entry : properties.entrySet()) {
            final ModelNode address = Operations.createAddress("system-property", entry.getKey());
            final ModelNode op = Operations.createAddOperation(address);
            op.get("value").set(entry.getValue());
            builder.addStep(op);
        }
        executeOperation(managementClient, builder.build());
    }

    @Override
    public void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
        final Operations.CompositeOperationBuilder builder = Operations.CompositeOperationBuilder.create();
        for (var entry : properties.entrySet()) {
            final ModelNode address = Operations.createAddress("system-property", entry.getKey());
            builder.addStep(Operations.createRemoveOperation(address));
        }
        executeOperation(managementClient, builder.build());
    }
}

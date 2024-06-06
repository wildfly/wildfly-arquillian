/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.junit5.server.setup;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.arquillian.setup.SystemPropertyServerSetupTask;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.wildfly.arquillian.junit.annotations.WildFlyArquillian;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@WildFlyArquillian
@RunAsClient
abstract class SetupTaskTests {

    public static class SuccessfulSetupTask extends SystemPropertyServerSetupTask implements ServerSetupTask {
        public static final String PROPERTY_NAME = "wildfly.arquillian.test.success";

        public SuccessfulSetupTask() {
            super(Map.of(PROPERTY_NAME, "true"));
        }
    }

    public static class AfterSuccessfulSetupTask extends SystemPropertyServerSetupTask implements ServerSetupTask {
        public static final String PROPERTY_NAME = "wildfly.arquillian.test.success.after";

        public AfterSuccessfulSetupTask() {
            super(Map.of(PROPERTY_NAME, "true"));
        }
    }

    public static class RuntimeExceptionSetupTask extends SystemPropertyServerSetupTask implements ServerSetupTask {
        public static final String PROPERTY_NAME = "wildfly.arquillian.test.runtime.exception";

        public RuntimeExceptionSetupTask() {
            super(Map.of(PROPERTY_NAME, "true"));
        }

        @Override
        public void setup(final ManagementClient managementClient, final String containerId) throws Exception {
            super.setup(managementClient, containerId);
            throw new RuntimeException("RuntimeException failed on purpose");
        }
    }

    public static class AssertionErrorSetupTask extends SystemPropertyServerSetupTask implements ServerSetupTask {
        public static final String PROPERTY_NAME = "wildfly.arquillian.test.assertion.error";

        public AssertionErrorSetupTask() {
            super(Map.of(PROPERTY_NAME, "true"));
        }

        @Override
        public void setup(final ManagementClient managementClient, final String containerId) throws Exception {
            super.setup(managementClient, containerId);
            Assertions.fail("AssertionError failed on purpose");
        }
    }

    @ArquillianResource
    private ManagementClient client;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "inner-setup-task-tests.war")
                .addClasses(SetupTaskTestCase.class,
                        SuccessfulSetupTask.class,
                        RuntimeExceptionSetupTask.class,
                        AssertionErrorSetupTask.class);
    }

    @Test
    public void failIfExecuted(final TestInfo testInfo) {
        Assertions.fail(String.format("Test %s.%s should not have been executed.",
                testInfo.getTestClass().map(Class::getName).orElse("Unknown"),
                testInfo.getTestMethod().map(Method::getName).orElse("Unknown")));
    }

    @Test
    public void systemPropertiesExist() throws Exception {
        final Set<String> properties = getSystemProperties()
                .stream()
                .map(ModelNode::asString)
                .collect(Collectors.toCollection(TreeSet::new));
        Assertions.assertIterableEquals(
                new TreeSet<>(Set.of(SuccessfulSetupTask.PROPERTY_NAME, AfterSuccessfulSetupTask.PROPERTY_NAME)), properties);
    }

    private List<ModelNode> getSystemProperties() throws IOException {
        final ModelNode op = Operations.createOperation("read-children-names");
        op.get(ClientConstants.CHILD_TYPE).set("system-property");
        return executeOperation(op).asList();
    }

    private ModelNode executeOperation(final ModelNode op) throws IOException {
        final ModelNode result = client.getControllerClient().execute(op);
        if (!Operations.isSuccessfulOutcome(result)) {
            Assertions.fail("Operation has failed: " + Operations.getFailureDescription(result).asString());
        }
        return Operations.readResult(result);
    }

    @ServerSetup({
            SuccessfulSetupTask.class,
            RuntimeExceptionSetupTask.class,
            AfterSuccessfulSetupTask.class
    })
    public static class SuccessThenRuntimeFail extends SetupTaskTests {
    }

    @ServerSetup({
            SuccessfulSetupTask.class,
            AssertionErrorSetupTask.class,
            AfterSuccessfulSetupTask.class
    })
    public static class SuccessThenAssertionFail extends SetupTaskTests {
    }

    @ServerSetup({
            SuccessfulSetupTask.class,
            AfterSuccessfulSetupTask.class
    })
    public static class SuccessAndAfter extends SetupTaskTests {
    }
}

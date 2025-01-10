/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.junit5.server.setup;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.client.helpers.Operations.CompositeOperationBuilder;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.EventConditions;
import org.junit.platform.testkit.engine.TestExecutionResultConditions;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ArquillianTest
@RunAsClient
public class SetupTaskTestCase {

    @ArquillianResource
    private ManagementClient client;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "setup-task-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @BeforeEach
    public void clearSystemProperties() throws Exception {
        final CompositeOperationBuilder builder = CompositeOperationBuilder.create();
        for (ModelNode name : getSystemProperties()) {
            final ModelNode address = Operations.createAddress("system-property", name.asString());
            builder.addStep(Operations.createRemoveOperation(address));
        }
        executeOperation(builder.build());
    }

    @Test
    public void successThenAssertionFail() throws Exception {
        final var results = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectClass(SetupTaskTests.SuccessThenAssertionFail.class))
                .execute();
        // No tests should have been executed
        final var testEvents = results.testEvents();
        testEvents.assertThatEvents().isEmpty();
        // We should have one failure from the setup task
        final var events = results.allEvents();
        events.assertStatistics((stats) -> stats.failed(1L));
        events.assertThatEvents()
                .haveAtLeastOne(EventConditions.event(
                        EventConditions.finishedWithFailure(TestExecutionResultConditions.instanceOf(AssertionError.class))));
        assertOnlyProperties(SetupTaskTests.AssertionErrorSetupTask.PROPERTY_NAME);
    }

    @Test
    public void successThenRuntimeFail() throws Exception {
        final var results = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectClass(SetupTaskTests.SuccessThenRuntimeFail.class))
                .execute();
        // No tests should have been executed
        final var testEvents = results.testEvents();
        testEvents.assertThatEvents().isEmpty();
        // We should have one failure from the setup task
        final var events = results.allEvents();
        events.assertStatistics((stats) -> stats.failed(1L));
        events.assertThatEvents()
                .haveAtLeastOne(EventConditions.event(
                        EventConditions.finishedWithFailure(TestExecutionResultConditions.instanceOf(RuntimeException.class))));
        assertOnlyProperties(SetupTaskTests.RuntimeExceptionSetupTask.PROPERTY_NAME);
    }

    @Test
    public void successAndAfter() throws Exception {
        final var results = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(SetupTaskTests.SuccessAndAfter.class, "systemPropertiesExist"))
                .execute();
        // The test should have been successful
        final var testEvents = results.testEvents();
        testEvents.assertThatEvents().haveExactly(1, EventConditions.finishedSuccessfully());
        // All properties should have been removed in the SetupServerTask.tearDown()
        assertNoSystemProperties();
    }

    private void assertOnlyProperties(final String... names) throws IOException {
        final Set<String> expectedNames = Set.of(names);
        final Set<String> allProperties = getSystemProperties()
                .stream()
                .map(ModelNode::asString)
                .collect(Collectors.toCollection(LinkedHashSet<String>::new));
        Assertions.assertTrue(allProperties.containsAll(expectedNames), () -> String
                .format("The following properties were expected in \"%s\", but not found; %s", allProperties, expectedNames));
        // Remove the expected properties
        allProperties.removeAll(expectedNames);
        Assertions.assertTrue(allProperties.isEmpty(),
                () -> String.format("The following properties exist which should not exist: %s", allProperties));
    }

    private void assertNoSystemProperties() throws IOException {
        final ModelNode op = Operations.createOperation("read-children-names");
        op.get(ClientConstants.CHILD_TYPE).set("system-property");
        final ModelNode result = executeOperation(op);
        Assertions.assertTrue(result.asList()
                .isEmpty(), () -> "Expected no system properties, found: " + result.asString());
    }

    private List<ModelNode> getSystemProperties() throws IOException {
        final ModelNode op = Operations.createOperation("read-children-names");
        op.get(ClientConstants.CHILD_TYPE).set("system-property");
        return executeOperation(op).asList();
    }

    private ModelNode executeOperation(final ModelNode op) throws IOException {
        return executeOperation(Operation.Factory.create(op));
    }

    private ModelNode executeOperation(final Operation op) throws IOException {
        final ModelNode result = client.getControllerClient().execute(op);
        if (!Operations.isSuccessfulOutcome(result)) {
            Assertions.fail("Operation has failed: " + Operations.getFailureDescription(result).asString());
        }
        return Operations.readResult(result);
    }
}

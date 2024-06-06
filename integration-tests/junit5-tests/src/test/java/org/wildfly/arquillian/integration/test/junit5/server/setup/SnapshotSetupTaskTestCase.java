/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.junit5.server.setup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.arquillian.setup.SnapshotServerSetupTask;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.EventConditions;
import org.junit.platform.testkit.engine.TestExecutionResultConditions;
import org.wildfly.arquillian.junit.annotations.WildFlyArquillian;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@WildFlyArquillian
@RunAsClient
public class SnapshotSetupTaskTestCase {
    private static final String PROPERTY_NAME = SnapshotServerSetupTask.class.getName();
    private static final ModelNode ADDRESS = Operations.createAddress("system-property", PROPERTY_NAME);
    private static final Path TEST_FILE = Path.of(System.getProperty("java.io.tmpdir"), "snapshot-test-file.txt");

    @ArquillianResource
    private ManagementClient client;

    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "snapshot-setup-task-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void checkSnapshotRestored() throws Exception {
        final var results = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectClass(SuccessfulSnapshotTests.class))
                .execute();
        // Two tests should have been executed successfully
        final var testEvents = results.testEvents();
        testEvents.assertThatEvents().haveExactly(2, EventConditions.finishedSuccessfully());
        checkSystemProperty(client, false);
        Assertions.assertTrue(Files.notExists(TEST_FILE),
                "The test file should have been cleaned up in the nonManagementCleanup()");
    }

    @Test
    public void checkSnapshotRestoredRollback() throws Exception {
        final var results = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectClass(RollbackSnapshotTests.class))
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
        checkSystemProperty(client, false);
    }

    private static void checkSystemProperty(final ManagementClient client, final boolean expectSuccess) throws IOException {
        final ModelNode op = Operations.createReadAttributeOperation(ADDRESS, "value");
        final ModelNode result = client.getControllerClient().execute(op);
        final Supplier<String> msg;
        if (expectSuccess) {
            msg = () -> "Getting the system property failed: " + Operations.getFailureDescription(result)
                    .asString();
        } else {
            msg = () -> String.format("Expected system property %s to not exist.", PROPERTY_NAME);
        }
        Assertions.assertTrue((expectSuccess == Operations.isSuccessfulOutcome(result)), msg);
    }

    public static class TestSystemPropertySetupTask extends SnapshotServerSetupTask {
        @Override
        protected void doSetup(final ManagementClient managementClient, final String containerId) throws Exception {
            final ModelNode op = Operations.createAddOperation(ADDRESS);
            op.get("value").set("present");
            executeOperation(managementClient, op);
            Files.createFile(TEST_FILE);
        }

        @Override
        protected void nonManagementCleanUp() throws Exception {
            Files.delete(TEST_FILE);
        }
    }

    public static class ErrorSetupTask extends SnapshotServerSetupTask {
        @Override
        protected void doSetup(final ManagementClient managementClient, final String containerId) throws Exception {
            final ModelNode op = Operations.createAddOperation(ADDRESS);
            op.get("value").set("present");
            executeOperation(managementClient, op);
            Files.createFile(TEST_FILE);
            Assertions.fail("Failed on purpose");
        }

        @Override
        protected void nonManagementCleanUp() throws Exception {
            Files.delete(TEST_FILE);
        }
    }

    @WildFlyArquillian
    @RunAsClient
    public abstract static class SnapshotTests {

        @ArquillianResource
        private ManagementClient client;

        @Deployment(testable = false)
        public static WebArchive createDeployment() {
            return ShrinkWrap.create(WebArchive.class, "inner-snapshot-setup-task-test.war")
                    .addClass(SnapshotServerSetupTask.class)
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        }

        @Test
        public void systemPropertyExists() throws Exception {
            checkSystemProperty(client, true);
        }

        @Test
        public void fileExists() {
            Assertions.assertTrue(Files.exists(TEST_FILE), () -> "Expected test file to exist: " + TEST_FILE);
        }
    }

    @ServerSetup(TestSystemPropertySetupTask.class)
    public static class SuccessfulSnapshotTests extends SnapshotTests {

    }

    @ServerSetup(ErrorSetupTask.class)
    public static class RollbackSnapshotTests extends SnapshotTests {

    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.junit5.server.setup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.arquillian.container.managed.setup.CreateModuleServerSetupTask;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.OS;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.EventConditions;
import org.wildfly.arquillian.junit.annotations.WildFlyArquillian;
import org.wildfly.testing.tools.deployments.DeploymentDescriptors;
import org.wildfly.testing.tools.modules.ModuleBuilder;
import org.wildfly.testing.tools.modules.ModuleDescription;
import org.wildfly.testing.tools.modules.Modules;

/**
 * Tests the {@link CreateModuleServerSetupTask} works as expected.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@WildFlyArquillian
@RunAsClient
public class CreateModuleServerSetupTaskTestCase {
    private static final String MODULE_NAME = "org.wildfly.arquillian.test";
    private static final String MODULE_NAME_ALIAS = "org.wildfly.arquillian.test.alias";
    /**
     * A marker file to indicate this test has been executed
     */
    static final Path MARKER_FILE = Path.of(System.getProperty("java.io.tmpdir"),
            CreateModuleServerSetupTaskTestCase.class.getName() + "-marker");

    @ArquillianResource
    private ManagementClient client;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "module-setup-task-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @BeforeAll
    public static void deleteMarkerFile() throws IOException {
        Files.deleteIfExists(MARKER_FILE);
    }

    /**
     * Invokes a test method which checks that the module was loaded and is accessible. It then verifies that module is
     * no longer loaded in the container and that the module path as been deleted.
     *
     * @throws Exception if an error occurs
     */
    @Test
    public void checkModuleInstalled() throws Exception {
        Files.createFile(MARKER_FILE);
        final var results = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectClass(ModuleSetupTask.class))
                .execute();
        final var testEvents = results.allEvents();
        // We should have no failed events
        testEvents.assertThatEvents().haveExactly(0, EventConditions.finishedWithFailure());
        checkModule(client, false);
        // On Windows this will fail as the resources have not be released. The CreateModuleCleanUpTestCase should test
        // also be testing this.
        if (OS.current() != OS.WINDOWS) {
            assertModuleDeleted();
        }
    }

    static void assertModuleDeleted() {
        // Ensure the module no longer exists on the file system
        final var moduleDir = Modules.discoverModulePath().resolve(MODULE_NAME.replace('.', File.separatorChar))
                .resolve("main");
        Assertions.assertFalse(Files.exists(moduleDir), () -> String
                .format("Expected module %s to be deleted but is still present on file system: %s", MODULE_NAME, moduleDir));
        // Ensure the module no longer exists on the file system
        final var moduleAliasDir = Modules.discoverModulePath().resolve(MODULE_NAME_ALIAS.replace('.', File.separatorChar))
                .resolve("main");
        Assertions.assertFalse(Files.exists(moduleAliasDir), () -> String
                .format("Expected module %s to be deleted but is still present on file system: %s", MODULE_NAME_ALIAS,
                        moduleAliasDir));
    }

    private static void checkModule(final ManagementClient client, final boolean exists)
            throws Exception {
        try (JMXConnector connector = createJmxConnector(client)) {
            final MBeanServerConnection connection = connector.getMBeanServerConnection();
            boolean found = false;
            for (var mbean : connection.queryMBeans(ObjectName.getInstance("jboss.modules:type=ModuleLoader,name=*"), null)) {
                final List<String> info = List
                        .of((String[]) connection.invoke(mbean.getObjectName(), "queryLoadedModuleNames", null, null));
                if (exists) {
                    found = info.contains(MODULE_NAME);
                    if (found)
                        break;
                } else {
                    Assertions.assertFalse(info.contains(MODULE_NAME),
                            () -> String.format("Module %s still exists", MODULE_NAME));
                }
            }
            Assertions.assertEquals(exists, found, () -> String.format("Failed to find module %s", MODULE_NAME));
        }
    }

    private static JMXConnector createJmxConnector(final ManagementClient client) throws IOException {
        final Map<String, Object> env = Map.of("org.jboss.remoting-jmx.timeout", "600");
        return JMXConnectorFactory.connect(client.getRemoteJMXURL(), env);
    }

    public static class TestModuleServerSetupTask extends CreateModuleServerSetupTask {

        @Override
        protected Set<ModuleDescription> moduleDescriptions() {
            return Set.of(
                    ModuleBuilder.of(MODULE_NAME)
                            .addClass(Greeter.class)
                            .addDependencies("org.jboss.as.server").build(),
                    ModuleDescription.createAlias(MODULE_NAME_ALIAS, MODULE_NAME));
        }
    }

    @WildFlyArquillian
    @ServerSetup(CreateModuleServerSetupTaskTestCase.TestModuleServerSetupTask.class)
    public static class ModuleSetupTask {

        @ArquillianResource
        private ManagementClient client;

        @Deployment(testable = false)
        public static WebArchive createDeployment() {
            return ShrinkWrap.create(WebArchive.class, "inner-module-setup-task-test.war")
                    .addClass(TestModuleServerSetupTask.class)
                    .addAsWebInfResource(DeploymentDescriptors.createJBossDeploymentStructureAsset(
                            Set.of(MODULE_NAME_ALIAS), Set.of()), "jboss-deployment-structure.xml")
                    .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        }

        @Test
        public void greet() throws Exception {
            checkModule(client, true);
            final Greeter greeter = new Greeter();
            Assertions.assertEquals("Hello Me", greeter.greet("Me"));
        }
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed.setup;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;

import org.jboss.as.arquillian.api.ReloadIfRequired;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.logging.Logger;
import org.wildfly.testing.tools.modules.ModuleDescription;

/**
 * A setup task which creates a module and deletes it when {@linkplain #tearDown(ManagementClient, String) torn down}.
 * If deleting the module fails, likely to happen on Windows given the resources are in-use by the class loader, a
 * {@linkplain Runtime#addShutdownHook(Thread) shutdown hook} will be added to delete the module after the JVM the tes
 * is executing in terminates.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @since 5.1
 */
@SuppressWarnings({ "unused", "RedundantThrows" })
@ReloadIfRequired
public abstract class CreateModuleServerSetupTask implements ServerSetupTask {
    private static final Logger LOGGER = Logger.getLogger(CreateModuleServerSetupTask.class);
    private final Set<ModuleDescription> modules = new ConcurrentSkipListSet<>();

    @Override
    public final void setup(final ManagementClient managementClient, final String containerId) throws Exception {
        modules.addAll(moduleDescriptions());
        doSetup(managementClient, containerId, Set.copyOf(modules));
    }

    @Override
    public final void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
        try {
            try (JMXConnector connector = createJmxConnector(managementClient)) {
                unloadModule(connector.getMBeanServerConnection(), modules.stream()
                        .map(ModuleDescription::name)
                        .collect(Collectors.toSet()));
            }
            for (ModuleDescription module : modules) {
                module.close();
            }
        } finally {
            doTearDown(managementClient, containerId);
        }
    }

    /**
     * Execute any necessary setup work that needs to happen before the first deployment to the given container. If
     * the server is in a state of reload-required, a reload will automatically be executed.
     *
     * @param managementClient management client to use to interact with the container
     * @param containerId      id of the container to which the deployment will be deployed
     * @param modules          the build modules for this setup
     *
     * @throws Exception if a failure occurs
     * @see #setup(ManagementClient, String)
     */
    protected void doSetup(final ManagementClient managementClient, final String containerId,
            final Set<ModuleDescription> modules) throws Exception {
    }

    /**
     * Execute any tear down work that needs to happen after the last deployment associated with the given container has
     * been undeployed. If the server is in a state of reload-required, a reload will automatically be executed.
     *
     * @param managementClient management client to use to interact with the container
     * @param containerId      id of the container to which the deployment will be deployed
     *
     * @throws Exception if a failure occurs
     * @see #tearDown(ManagementClient, String)
     */
    protected void doTearDown(final ManagementClient managementClient, final String containerId) throws Exception {
    }

    /**
     * The modules that should be created prior to a deployment.
     *
     * @return a set of modules to create
     */
    protected abstract Set<ModuleDescription> moduleDescriptions();

    private static void unloadModule(final MBeanServerConnection connection, final Set<String> moduleNames)
            throws MalformedObjectNameException, IOException, ReflectionException, InstanceNotFoundException, MBeanException {
        for (var mbean : connection.queryMBeans(ObjectName.getInstance("jboss.modules:type=ModuleLoader,name=*"), null)) {
            final String[] info = (String[]) connection.invoke(mbean.getObjectName(), "queryLoadedModuleNames", null, null);
            for (String module : info) {
                if (moduleNames.contains(module)) {
                    // We need to unload the module so it can be deleted
                    if (!((boolean) connection.invoke(mbean.getObjectName(), "unloadModule", new Object[] { module },
                            new String[] { "java.lang.String" }))) {
                        LOGGER.errorf("Failed to unload module: %s", module);
                    }
                }
            }
        }
    }

    private static JMXConnector createJmxConnector(final ManagementClient managementClient) throws IOException {
        final Map<String, Object> env = Map.of("org.jboss.remoting-jmx.timeout", "600");
        return JMXConnectorFactory.connect(managementClient.getRemoteJMXURL(), env);
    }

}

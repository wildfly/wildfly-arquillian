/*
 * Copyright 2015 Red Hat, Inc.
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

package org.jboss.as.arquillian.service;

import static org.jboss.as.server.deployment.Services.JBOSS_DEPLOYMENT;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.Consumer;
import javax.management.MBeanServer;

import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.protocol.jmx.JMXTestRunner;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.as.arquillian.protocol.jmx.ExtendedJMXProtocolConfiguration;
import org.jboss.as.jmx.MBeanServerService;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.Phase;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.msc.Service;
import org.jboss.msc.service.LifecycleEvent;
import org.jboss.msc.service.LifecycleListener;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.wildfly.security.manager.WildFlySecurityManager;

/**
 * Service responsible for creating and managing the life-cycle of the Arquillian service.
 *
 * @author Thomas.Diesler@jboss.com
 * @author Kabir Khan
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public class ArquillianService implements Service {

    public static final String TEST_CLASS_PROPERTY = "org.jboss.as.arquillian.testClass";
    public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("arquillian", "testrunner");
    private static final Logger log = Logger.getLogger("org.jboss.as.arquillian");

    private final Supplier<MBeanServer> mBeanServerSupplier;
    private final Consumer<ArquillianService> arquillianServiceConsumer;
    private final Set<ArquillianConfig> deployedTests = new HashSet<>();
    private volatile JMXTestRunner jmxTestRunner;
    private volatile LifecycleListener listener;

    private ArquillianService(final Supplier<MBeanServer> mBeanServerSupplier, final Consumer<ArquillianService> arquillianServiceConsumer) {
        this.mBeanServerSupplier = mBeanServerSupplier;
        this.arquillianServiceConsumer = arquillianServiceConsumer;
    }

    public static void addService(final ServiceTarget serviceTarget) {
        ServiceBuilder<?> builder = serviceTarget.addService(ArquillianService.SERVICE_NAME);
        builder.setInstance(new ArquillianService(
                builder.requires(MBeanServerService.SERVICE_NAME),
                builder.provides(ArquillianService.SERVICE_NAME)
        ));
        builder.install();
    }

    public synchronized void start(final StartContext context) throws StartException {
        log.debugf("Starting Arquillian Test Runner");

        arquillianServiceConsumer.accept(this);
        final MBeanServer mbeanServer = mBeanServerSupplier.get();
        try {
            jmxTestRunner = new ExtendedJMXTestRunner();
            jmxTestRunner.registerMBean(mbeanServer);
        } catch (Throwable t) {
            throw new StartException("Failed to start Arquillian Test Runner", t);
        }

        listener = new ArquillianListener(context.getChildTarget());
        context.getController().getServiceContainer().addListener(listener);
    }

    public synchronized void stop(final StopContext context) {
        log.debugf("Stopping Arquillian Test Runner");

        try {
            if (jmxTestRunner != null) {
                jmxTestRunner.unregisterMBean(mBeanServerSupplier.get());
            }
        } catch (Exception ex) {
            log.errorf(ex, "Cannot stop Arquillian Test Runner");
        }

        context.getController().getServiceContainer().removeListener(listener);
    }

    void registerArquillianConfig(final ArquillianConfig arqConfig) {
        synchronized (deployedTests) {
            log.debugf("Register Arquillian config: %s", arqConfig.getServiceName());
            deployedTests.add(arqConfig);
            deployedTests.notifyAll();
        }
    }

    void unregisterArquillianConfig(final ArquillianConfig arqConfig) {
        synchronized (deployedTests) {
            log.debugf("Unregister Arquillian config: %s", arqConfig.getServiceName());
            deployedTests.remove(arqConfig);
        }
    }

    private ArquillianConfig getArquillianConfig(final String className, final long timeout) {
        synchronized (deployedTests) {
            log.debugf("Getting Arquillian config for: %s", className);
            for (ArquillianConfig arqConfig : deployedTests) {
                for (String aux : arqConfig.getTestClasses()) {
                    if (aux.equals(className)) {
                        log.debugf("Found Arquillian config for: %s", className);
                        return arqConfig;
                    }
                }
            }

            if (timeout <= 0) {
                throw new IllegalStateException("Cannot obtain Arquillian config for: " + className);
            }

            try {
                log.debugf("Waiting on Arquillian config for: %s", className);
                deployedTests.wait(timeout);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        return getArquillianConfig(className, -1);
    }

    private class ExtendedJMXTestRunner extends JMXTestRunner {

        ExtendedJMXTestRunner() {
            super(new ExtendedTestClassLoader());
        }

        @Override
        public byte[] runTestMethod(final String className, final String methodName, Map<String, String> protocolProps) {
            // Setup the ContextManager
            ArquillianConfig config = getArquillianConfig(className, 30000L);
            Map<String, Object> properties = Collections.singletonMap(TEST_CLASS_PROPERTY, className);
            ContextManager contextManager = setupContextManager(config, properties);
            try {
                ClassLoader runWithClassLoader = ClassLoader.getSystemClassLoader();
                if (Boolean.parseBoolean(protocolProps.get(ExtendedJMXProtocolConfiguration.PROPERTY_ENABLE_TCCL))) {
                    DeploymentUnit depUnit = config.getDeploymentUnit();
                    Module module = depUnit.getAttachment(Attachments.MODULE);
                    if (module != null) {
                        runWithClassLoader = module.getClassLoader();
                    }
                }
                ClassLoader tccl = WildFlySecurityManager.setCurrentContextClassLoaderPrivileged(runWithClassLoader);
                try {
                    return super.runTestMethod(className, methodName, protocolProps);
                } finally {
                    WildFlySecurityManager.setCurrentContextClassLoaderPrivileged(tccl);
                }
            } finally {
                if(contextManager != null) {
                    contextManager.teardown(properties);
                }
            }
        }

        @Override
        protected TestResult doRunTestMethod(TestRunner runner, Class<?> testClass, String methodName, Map<String, String> protocolProps) {
            ClassLoader runWithClassLoader = ClassLoader.getSystemClassLoader();
            if (Boolean.parseBoolean(protocolProps.get(ExtendedJMXProtocolConfiguration.PROPERTY_ENABLE_TCCL))) {
                ArquillianConfig config = getArquillianConfig(testClass.getName(), 30000L);
                DeploymentUnit depUnit = config.getDeploymentUnit();
                Module module = depUnit.getAttachment(Attachments.MODULE);
                if (module != null) {
                    runWithClassLoader = module.getClassLoader();
                }
            }
            ClassLoader tccl = WildFlySecurityManager.setCurrentContextClassLoaderPrivileged(runWithClassLoader);
            try {
                return super.doRunTestMethod(runner, testClass, methodName, protocolProps);
            } finally {
                WildFlySecurityManager.setCurrentContextClassLoaderPrivileged(tccl);
            }
        }

        private ContextManager setupContextManager(final ArquillianConfig config, final Map<String, Object> properties) {
            try {
                final DeploymentUnit depUnit = config.getDeploymentUnit();
                final ContextManagerBuilder builder = new ContextManagerBuilder(config).addAll(depUnit);
                ContextManager contextManager = builder.build();
                contextManager.setup(properties);
                return contextManager;
            } catch (Throwable t) {
                return null;
            }
        }
    }

    class ExtendedTestClassLoader implements JMXTestRunner.TestClassLoader {

        @Override
        public Class<?> loadTestClass(final String className) throws ClassNotFoundException {

            final ArquillianConfig arqConfig = getArquillianConfig(className, -1);
            if (arqConfig == null)
                throw new ClassNotFoundException("No Arquillian config found for: " + className);

            return arqConfig.loadClass(className);
        }
    }

    // TODO: This listener based solution is still too hacky. Proper integration should be:
    // TODO: 1) either parentController service will expose DU retrieval via public method
    // TODO: 2) or this listener based solution will be replaced with WildFly Extension based solution
    private static class ArquillianListener implements LifecycleListener {
        private final ServiceTarget serviceTarget;

        private ArquillianListener(final ServiceTarget serviceTarget) {
            this.serviceTarget = serviceTarget;
        }

        @Override
        public void handleEvent(final ServiceController<?> controller, final LifecycleEvent event) {
            final ServiceName serviceName = controller.getName();
            if (!JBOSS_DEPLOYMENT.isParentOf(serviceName)) return;
            final String simpleName = serviceName.getSimpleName();

            if (event == LifecycleEvent.DOWN && simpleName.equals(Phase.DEPENDENCIES.toString())) {
                // DOWN event can happen multiple times during service lifecycle so this is handled
                // in ArquillianConfigBuilder.handleParseAnnotations(depUnit) method below.
                ServiceName parentName = serviceName.getParent();
                ServiceController<?> parentController = controller.getServiceContainer().getService(parentName);
                DeploymentUnit depUnit = (DeploymentUnit) parentController.getValue(); // TODO: eliminate deprecated API usage
                ArquillianConfigBuilder.handleParseAnnotations(depUnit);
            } else if (event == LifecycleEvent.UP && simpleName.equals(Phase.INSTALL.toString())) {
                ServiceName parentName = serviceName.getParent();
                ServiceController<?> parentController = controller.getServiceContainer().getService(parentName);
                DeploymentUnit depUnit = (DeploymentUnit) parentController.getValue(); // TODO: eliminate deprecated API usage
                Set<String> testClasses = ArquillianConfigBuilder.getClasses(depUnit);
                if (testClasses != null) {
                    String duName = ArquillianConfigBuilder.getName(depUnit);
                    ServiceName arqConfigSN = ServiceName.JBOSS.append("arquillian", "config", duName);
                    ServiceBuilder<ArquillianConfig> builder = (ServiceBuilder<ArquillianConfig>) serviceTarget.addService(arqConfigSN);
                    ArquillianConfig arqConfig = new ArquillianConfig(arqConfigSN, testClasses,
                            builder.requires(ArquillianService.SERVICE_NAME),
                            builder.requires(parentController.getName()));
                    arqConfig.addDeps(builder, controller);
                    builder.setInstance(arqConfig);
                    builder.install();
                    log.infof("Arquillian deployment detected: %s", arqConfig);
                }
            }
        }
    }
}

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
import javax.management.MBeanServer;

import org.jboss.arquillian.container.test.spi.TestRunner;
import org.jboss.arquillian.protocol.jmx.JMXTestRunner;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.testenricher.msc.ServiceTargetAssociation;
import org.jboss.as.arquillian.protocol.jmx.ExtendedJMXProtocolConfiguration;
import org.jboss.as.jmx.MBeanServerService;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.Phase;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.msc.service.AbstractServiceListener;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.wildfly.security.manager.WildFlySecurityManager;

/**
 * Service responsible for creating and managing the life-cycle of the Arquillian service.
 *
 *
 * @author Thomas.Diesler@jboss.com
 * @author Kabir Khan
 * @since 17-Nov-2010
 */
public class ArquillianService implements Service<ArquillianService> {

    public static final String TEST_CLASS_PROPERTY = "org.jboss.as.arquillian.testClass";
    public static final ServiceName SERVICE_NAME = ServiceName.JBOSS.append("arquillian", "testrunner");

    private static final Logger log = Logger.getLogger("org.jboss.as.arquillian");

    private final InjectedValue<MBeanServer> injectedMBeanServer = new InjectedValue<MBeanServer>();
    private final Set<ArquillianConfig> deployedTests = new HashSet<>();
    private volatile JMXTestRunner jmxTestRunner;
    private volatile AbstractServiceListener<Object> listener;

    public static void addService(final ServiceTarget serviceTarget) {
        ArquillianService service = new ArquillianService();
        ServiceBuilder<?> builder = serviceTarget.addService(ArquillianService.SERVICE_NAME, service);
        builder.addDependency(MBeanServerService.SERVICE_NAME, MBeanServer.class, service.injectedMBeanServer);
        builder.setInitialMode(ServiceController.Mode.ACTIVE);
        builder.install();
    }

    public synchronized void start(StartContext context) throws StartException {
        log.debugf("Starting Arquillian Test Runner");

        final MBeanServer mbeanServer = injectedMBeanServer.getValue();
        try {
            jmxTestRunner = new ExtendedJMXTestRunner();
            jmxTestRunner.registerMBean(mbeanServer);
        } catch (Throwable t) {
            throw new StartException("Failed to start Arquillian Test Runner", t);
        }

        listener = new ArquillianServiceListener(context.getChildTarget());
        context.getController().getServiceContainer().addListener(listener);
    }

    public synchronized void stop(StopContext context) {
        log.debugf("Stopping Arquillian Test Runner");
        try {
            if (jmxTestRunner != null) {
                jmxTestRunner.unregisterMBean(injectedMBeanServer.getValue());
            }
        } catch (Exception ex) {
            log.errorf(ex, "Cannot stop Arquillian Test Runner");
        } finally {
            context.getController().getServiceContainer().removeListener(listener);

        }
    }

    @Override
    public synchronized ArquillianService getValue() throws IllegalStateException {
        return this;
    }

    void registerArquillianConfig(ArquillianConfig arqConfig) {
        synchronized (deployedTests) {
            log.debugf("Register Arquillian config: %s", arqConfig.getServiceName());
            deployedTests.add(arqConfig);
            deployedTests.notifyAll();
        }
    }

    void unregisterArquillianConfig(ArquillianConfig arqConfig) {
        synchronized (deployedTests) {
            log.debugf("Unregister Arquillian config: %s", arqConfig.getServiceName());
            deployedTests.remove(arqConfig);
        }
    }

    private ArquillianConfig getArquillianConfig(final String className, long timeout) {
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

    class ExtendedJMXTestRunner extends JMXTestRunner {

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
                    DeploymentUnit depUnit = config.getDeploymentUnit().getValue();
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
                DeploymentUnit depUnit = config.getDeploymentUnit().getValue();
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
                final DeploymentUnit depUnit = config.getDeploymentUnit().getValue();
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

    private static class ArquillianServiceListener extends AbstractServiceListener<Object> {
        private ServiceTarget serviceTarget;

        private ArquillianServiceListener(ServiceTarget serviceTarget) {
            this.serviceTarget = serviceTarget;
        }

        @Override
        public void transition(ServiceController<? extends Object> serviceController, ServiceController.Transition transition) {
            switch (transition.getAfter()) {
                case UP: {
                    ServiceName serviceName = serviceController.getName();
                    String simpleName = serviceName.getSimpleName();
                    if (JBOSS_DEPLOYMENT.isParentOf(serviceName) && simpleName.equals(Phase.INSTALL.toString())) {
                        ServiceName parentName = serviceName.getParent();
                        ServiceController<?> parentController = serviceController.getServiceContainer().getService(parentName);
                        DeploymentUnit depUnit = (DeploymentUnit) parentController.getValue();
                        ArquillianConfig arqConfig = ArquillianConfigBuilder.processDeployment(depUnit);
                        if (arqConfig != null) {
                            log.infof("Arquillian deployment detected: %s", arqConfig);
                            ServiceBuilder<ArquillianConfig> builder = serviceTarget.addService(arqConfig.getServiceName(), arqConfig)
                                    .addDependency(ArquillianService.SERVICE_NAME, ArquillianService.class, arqConfig.getArquillianService())
                                    .addDependency(parentController.getName(), DeploymentUnit.class, arqConfig.getDeploymentUnit());
                            arqConfig.addDeps(builder, serviceController);
                            builder.setInitialMode(ServiceController.Mode.ACTIVE);
                            builder.install();
                        }
                    }
                }
                break;
                case STARTING: {
                    ServiceName serviceName = serviceController.getName();
                    String simpleName = serviceName.getSimpleName();
                    if(JBOSS_DEPLOYMENT.isParentOf(serviceName) && simpleName.equals(Phase.DEPENDENCIES.toString())) {
                        ServiceName parentName = serviceName.getParent();
                        ServiceController<?> parentController = serviceController.getServiceContainer().getService(parentName);
                        DeploymentUnit depUnit = (DeploymentUnit) parentController.getValue();
                        ArquillianConfigBuilder.handleParseAnnotations(depUnit);
                    }
                }
                break;
            }
        }
    }
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jboss.arquillian.container.test.spi.util.ServiceLoader;
import org.jboss.arquillian.testenricher.msc.ServiceTargetAssociation;
import org.jboss.as.arquillian.protocol.jmx.TestDescription;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.modules.Module;
import org.jboss.msc.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;

/**
 * The ArquillianConfig represents an Arquillian deployment.
 *
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class ArquillianConfig implements Service {

    private final List<ArquillianConfigServiceCustomizer> serviceCustomizers = new ArrayList<>();
    private final Supplier<ArquillianService> arquillianServiceSupplier;
    private final Supplier<DeploymentUnit> deploymentUnitSupplier;
    private final ServiceName serviceName;
    private final Map<String, TestClassInfo> testClasses;

    ArquillianConfig(final ServiceName serviceName, final Map<String, TestClassInfo> testClasses,
            final Supplier<ArquillianService> arquillianServiceSupplier,
            final Supplier<DeploymentUnit> deploymentUnitSupplier) {
        this.serviceName = serviceName;
        this.testClasses = Map.copyOf(testClasses);
        this.arquillianServiceSupplier = arquillianServiceSupplier;
        this.deploymentUnitSupplier = deploymentUnitSupplier;
        for (ArquillianConfigServiceCustomizer customizer : ServiceLoader.load(ArquillianConfigServiceCustomizer.class)) {
            serviceCustomizers.add(customizer);
        }
    }

    void addDeps(ServiceBuilder<ArquillianConfig> builder, ServiceController<?> depController) {
        for (ArquillianConfigServiceCustomizer customizer : serviceCustomizers) {
            customizer.customizeService(this, builder, depController);
        }
    }

    DeploymentUnit getDeploymentUnit() {
        return deploymentUnitSupplier.get();
    }

    ServiceName getServiceName() {
        return serviceName;
    }

    /**
     * Gets whether this config supports the given test class.
     *
     * @param className the name of the test class. Cannot be {@code null}
     *
     * @return {@code true} if this config supports a test class with the given classname
     */
    boolean supports(String className) {
        return testClasses.containsKey(className);
    }

    /**
     * Gets whether this config supports the given test class and method. The method is considered supported
     * if the class either has no methods annotated with {@code OperateOnDeployment} or it has at least one
     * method with that annotation where the annotation value specifies this deployment.
     * <p>
     * Note that a return value of {@code true} does not guarantee that a method named {@code methodName} exists.
     *
     * @param className  the name of the test class. Cannot be {@code null}
     * @param methodName the name of the test class. Cannot be {@code null}
     *
     * @return {@code true} if this config supports a test class with the given classname and method name
     */
    boolean supports(String className, String methodName) {
        final TestClassInfo testClassInfo = testClasses.get(className);
        return testClassInfo != null && (getDeploymentUnit().getName()
                .equals(testClassInfo.testDescription.deploymentName())
                && testClassInfo.supportsMethod(methodName));
    }

    Class<?> loadClass(String className) throws ClassNotFoundException {
        if (!testClasses.containsKey(className))
            throw new ClassNotFoundException("Class '" + className + "' not found in: " + testClasses);

        final Module module = deploymentUnitSupplier.get().getAttachment(Attachments.MODULE);
        Class<?> testClass = module.getClassLoader().loadClass(className);

        for (ArquillianConfigServiceCustomizer customizer : serviceCustomizers) {
            customizer.customizeLoadClass(deploymentUnitSupplier.get(), testClass);
        }

        return testClass;
    }

    @Override
    public void start(final StartContext context) {
        arquillianServiceSupplier.get().registerArquillianConfig(this);
        for (final String testClass : testClasses.keySet()) {
            ServiceTargetAssociation.setServiceTarget(testClass, context.getChildTarget());
        }
    }

    @Override
    public void stop(final StopContext context) {
        context.getController().setMode(Mode.REMOVE);
        arquillianServiceSupplier.get().unregisterArquillianConfig(this);
        for (final String testClass : testClasses.keySet()) {
            ServiceTargetAssociation.clearServiceTarget(testClass);
        }
    }

    @Override
    public String toString() {
        final String uname = serviceName.getSimpleName();
        final String sname = serviceName.getCanonicalName();
        return "ArquillianConfig[service=" + sname + ",unit=" + uname + ",tests=" + testClasses + "]";
    }

    static final class TestClassInfo {

        private final boolean allMethods;
        private final TestDescription testDescription;
        private final Set<String> methods;

        TestClassInfo(final TestDescription testDescription) {
            this.allMethods = true;
            this.testDescription = testDescription;
            this.methods = Collections.emptySet();
        }

        TestClassInfo(final TestDescription testDescription, final Set<String> methods) {
            this.allMethods = false;
            this.testDescription = testDescription;
            this.methods = new HashSet<>(methods);
        }

        private boolean supportsMethod(final String methodName) {
            return allMethods || methods.contains(methodName);
        }
    }
}

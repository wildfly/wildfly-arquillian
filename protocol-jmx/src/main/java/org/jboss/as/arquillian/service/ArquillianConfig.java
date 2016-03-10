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
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.container.test.spi.util.ServiceLoader;
import org.jboss.arquillian.testenricher.msc.ServiceTargetAssociation;
import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * The ArquillianConfig represents an Arquillian deployment.
 *
 * @author Thomas.Diesler@jboss.com
 */
public class ArquillianConfig implements Service<ArquillianConfig> {

    private static final Logger log = Logger.getLogger(ArquillianConfig.class);

    static final AttachmentKey<ArquillianConfig> KEY = AttachmentKey.create(ArquillianConfig.class);

    private final List<ArquillianConfigServiceCustomizer> serviceCustomizers = new ArrayList<ArquillianConfigServiceCustomizer>();

    private final ArquillianService arqService;
    private final DeploymentUnit depUnit;
    private final ServiceName serviceName;
    private final List<String> testClasses = new ArrayList<String>();

    private ServiceTarget serviceTarget;

    static ServiceName getServiceName(DeploymentUnit depUnit) {
        return ServiceName.JBOSS.append("arquillian", "config", depUnit.getName());
    }

    ArquillianConfig(ArquillianService arqService, DeploymentUnit depUnit, Set<String> testClasses) {
        this.arqService = arqService;
        this.depUnit = depUnit;
        this.serviceName = getServiceName(depUnit);
        this.testClasses.addAll(testClasses);

        for(ArquillianConfigServiceCustomizer customizer : ServiceLoader.load(ArquillianConfigServiceCustomizer.class)) {
            serviceCustomizers.add(customizer);
        }
    }

    ServiceBuilder<ArquillianConfig> buildService(ServiceTarget serviceTarget, ServiceController<?> depController) {
        ServiceBuilder<ArquillianConfig> builder = serviceTarget.addService(getServiceName(), this);
        builder.addDependency(depController.getName());
        for(ArquillianConfigServiceCustomizer customizer : serviceCustomizers) {
            customizer.customizeService(this, builder, depController);
        }
        return builder;
    }

    ServiceName getServiceName() {
        return serviceName;
    }

    DeploymentUnit getDeploymentUnit() {
        return depUnit;
    }

    List<String> getTestClasses() {
        return Collections.unmodifiableList(testClasses);
    }

    Class<?> loadClass(String className) throws ClassNotFoundException {

        if (testClasses.contains(className) == false)
            throw new ClassNotFoundException("Class '" + className + "' not found in: " + testClasses);

        Module module = depUnit.getAttachment(Attachments.MODULE);
        Class<?> testClass = module.getClassLoader().loadClass(className);

        for(ArquillianConfigServiceCustomizer customizer : serviceCustomizers) {
            customizer.customizeLoadClass(depUnit, testClass);
        }

        ServiceTargetAssociation.setServiceTarget(serviceTarget);
        return testClass;
    }

    @Override
    public synchronized void start(StartContext context) throws StartException {
        serviceTarget = context.getChildTarget();
        arqService.registerArquillianConfig(this);
    }

    @Override
    public synchronized void stop(StopContext context) {
        context.getController().setMode(Mode.REMOVE);
        arqService.unregisterArquillianConfig(this);
    }

    @Override
    public synchronized ArquillianConfig getValue() {
        return this;
    }

    @Override
    public String toString() {
        String uname = depUnit.getName();
        String sname = serviceName.getCanonicalName();
        return "ArquillianConfig[service=" + sname + ",unit=" + uname + ",tests=" + testClasses + "]";
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.domain;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.AfterClass;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.arquillian.domain.api.TargetsServerGroup;
import org.wildfly.arquillian.domain.api.TargetsServerGroups;

/**
 * Watches for events to determine the target server group(s) for deployments.
 * <p>
 * Note that if any other Arquillian extensions also wrap the
 * {@linkplain DeploymentDescription#getTestableArchive() testable archive} this maybe break deployments.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ServerGroupDeploymentObserver {

    private final Map<String, Set<String>> serverGroupTargets = new HashMap<>();

    public synchronized void setTargetServerGroups(@Observes BeforeDeploy event) {
        final String deploymentName = event.getDeployment().getName();
        if (serverGroupTargets.containsKey(deploymentName)) {
            final DeploymentDescription deploymentDescription = event.getDeployment();
            final Archive<?> delegate = deploymentDescription.getArchive();
            // Note that this breaks if anything else replaces this archive
            deploymentDescription.setTestableArchive(
                    new ServerGroupArchive<>(delegate, Collections.unmodifiableSet(serverGroupTargets.get(deploymentName))));
        }
    }

    public synchronized void findTargetServerGroups(@Observes(precedence = 100) BeforeClass event) {
        final TestClass testClass = event.getTestClass();
        final Method[] methods = testClass.getMethods(Deployment.class);
        for (Method method : methods) {
            if (method.isAnnotationPresent(TargetsServerGroups.class)) {
                final Deployment deployment = method.getAnnotation(Deployment.class);
                for (TargetsServerGroup target : method.getAnnotation(TargetsServerGroups.class).value()) {
                    add(deployment.name(), target.value());
                }

            } else if (method.isAnnotationPresent(TargetsServerGroup.class)) {
                final Deployment deployment = method.getAnnotation(Deployment.class);
                final TargetsServerGroup target = method.getAnnotation(TargetsServerGroup.class);
                add(deployment.name(), target.value());
            }
        }
    }

    public synchronized void removeTargertServerGroups(@Observes AfterClass event) {
        final TestClass testClass = event.getTestClass();
        final Method[] methods = testClass.getMethods(Deployment.class);
        for (Method method : methods) {
            final Deployment deployment = method.getAnnotation(Deployment.class);
            serverGroupTargets.remove(deployment.name());
        }
    }

    private void add(final String deploymentName, final String serverGroup) {
        Set<String> serverGroups = serverGroupTargets.get(deploymentName);
        if (serverGroups == null) {
            serverGroups = new LinkedHashSet<>();
            serverGroupTargets.put(deploymentName, serverGroups);
        }
        serverGroups.add(serverGroup);
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.as.arquillian.protocol.jmx.TestDescription;
import org.jboss.as.server.deployment.AttachmentKey;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

/**
 * Uses the annotation index to check whether there is a class annotated
 * with JUnit @RunWith, or extending from the TestNG Arquillian runner.
 * In which case an {@link ArquillianConfig} service is created.
 *
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
class ArquillianConfigBuilder {

    private static final Logger log = Logger.getLogger("org.jboss.as.arquillian");

    /*
     * Note: Do not put direct class references on JUnit or TestNG here; this
     * must be compatible with both without resulting in NCDFE
     *
     * AS7-1303
     */

    private static final String CLASS_NAME_JUNIT_RUNNER = "org.junit.runner.RunWith";

    private static final String CLASS_NAME_JUNIT5_RUNNER = "org.junit.jupiter.api.extension.ExtendWith";

    private static final String CLASS_NAME_TESTNG_RUNNER = "org.jboss.arquillian.testng.Arquillian";

    private static final DotName OPERATE_ON_DEPLOYMENT = DotName
            .createSimple("org.jboss.arquillian.container.test.api.OperateOnDeployment");

    private static final AttachmentKey<Map<String, ArquillianConfig.TestClassInfo>> CLASSES = AttachmentKey
            .create(Map.class);

    ArquillianConfigBuilder() {
    }

    static Map<String, ArquillianConfig.TestClassInfo> getClasses(final DeploymentUnit depUnit) {
        // Get Test Class Names
        final Map<String, ArquillianConfig.TestClassInfo> testClasses = depUnit.getAttachment(CLASSES);
        return testClasses == null || testClasses.isEmpty() ? null : testClasses;
    }

    static String getName(final DeploymentUnit depUnit) {
        String depUnitName = depUnit.getName();
        DeploymentUnit parent;
        if ((parent = depUnit.getParent()) != null) {
            depUnitName = parent.getName() + "." + depUnitName;
        }
        return depUnitName;
    }

    static void handleParseAnnotations(final DeploymentUnit deploymentUnit) {

        final CompositeIndex compositeIndex = deploymentUnit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);
        if (compositeIndex == null) {
            log.warnf("Cannot find composite annotation index in: %s", deploymentUnit);
            return;
        }
        if (deploymentUnit.hasAttachment(CLASSES)) {
            // this hack is needed because ArquillianListener.handleEvent() method
            // DOWN event can happen multiple times during service lifecycle.
            return;
        }

        // Got JUnit?
        final DotName runWithName = DotName.createSimple(CLASS_NAME_JUNIT_RUNNER);
        final List<AnnotationInstance> runWithList = new ArrayList<>(compositeIndex.getAnnotations(runWithName));

        // JUnit 5
        final DotName extendWith = DotName.createSimple(CLASS_NAME_JUNIT5_RUNNER);
        runWithList.addAll(compositeIndex.getAnnotations(extendWith));

        // Got TestNG?
        final DotName testNGClassName = DotName.createSimple(CLASS_NAME_TESTNG_RUNNER);
        final Set<ClassInfo> testNgTests = compositeIndex.getAllKnownSubclasses(testNGClassName);

        // Get Test Class Names
        final Map<String, ArquillianConfig.TestClassInfo> testClasses = new LinkedHashMap<>();
        final TestDescription testDescription = TestDescription.from(deploymentUnit);
        // JUnit
        for (AnnotationInstance instance : runWithList) {
            final AnnotationTarget target = instance.target();
            if (target instanceof ClassInfo) {
                final ClassInfo classInfo = (ClassInfo) target;
                final String testClassName = classInfo.name().toString();
                testClasses.put(testClassName,
                        getTestMethods(compositeIndex, classInfo, testDescription));
            }
        }
        // TestNG
        for (final ClassInfo classInfo : testNgTests) {
            testClasses.put(classInfo.name().toString(), getTestMethods(compositeIndex, classInfo, testDescription));
        }
        deploymentUnit.putAttachment(CLASSES, testClasses);
    }

    private static ArquillianConfig.TestClassInfo getTestMethods(final CompositeIndex compositeIndex, final ClassInfo classInfo,
            final TestDescription testDescription) {
        // Record all methods which can operate on this deployment.
        final Set<String> methods = new HashSet<>();
        final String deploymentName = testDescription.arquillianDeploymentName().orElse(null);
        findAllMethods(compositeIndex, classInfo, deploymentName, methods);
        return new ArquillianConfig.TestClassInfo(testDescription, Set.copyOf(methods));
    }

    private static void findAllMethods(final CompositeIndex compositeIndex, final ClassInfo classInfo,
            final String deploymentName, final Set<String> methods) {
        if (classInfo == null) {
            return;
        }
        classInfo.methods().forEach(methodInfo -> {
            // If the @OperateOnDeployment method is present, it must match the test descriptions deployment
            if (methodInfo.hasAnnotation(OPERATE_ON_DEPLOYMENT)) {
                final AnnotationInstance annotation = methodInfo.annotation(OPERATE_ON_DEPLOYMENT);
                if (annotation.value().asString().equals(deploymentName)) {
                    methods.add(methodInfo.name());
                }
            } else {
                // No @OperateOnDeployment annotation present on the method, we have to assume it's okay to run for
                // this test description.
                methods.add(methodInfo.name());
            }
        });
        if (classInfo.superName() != null && !classInfo.superName().toString().equals(Object.class.getName())) {
            findAllMethods(compositeIndex, compositeIndex.getClassByName(classInfo.superName()), deploymentName, methods);
        }
        // Interfaces can have default methods, we'll check those too
        classInfo.interfaceNames()
                .forEach(name -> findAllMethods(compositeIndex, compositeIndex.getClassByName(name), deploymentName, methods));
    }
}

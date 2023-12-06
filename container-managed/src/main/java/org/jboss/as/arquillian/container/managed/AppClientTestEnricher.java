/*
 * Copyright 2023 Red Hat, Inc.
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

package org.jboss.as.arquillian.container.managed;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.spi.TestEnricher;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A TestEnricher that supports the injection of the AppClientWrapper application client container runner
 */
public class AppClientTestEnricher implements TestEnricher {
    @Inject
    private Instance<ContainerContext> containerContext;

    @Inject
    private Instance<ContainerRegistry> containerRegistry;

    /**
     * Support injection of the AppClientWrapper into a test instance
     * @param testCase - test instance
     */
    @Override
    public void enrich(Object testCase) {
        AppClientWrapper appClient = getAppClient();
        if(appClient != null) {
            List<Field> appClientFields = getAppClientFields(testCase);
            for(Field f : appClientFields) {
                try {
                    f.set(testCase, appClient);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Could not set value on field " + f + " using " + testCase);
                }
            }
        }
    }

    /**
     * Support injection of the AppClientWrapper into a test method
     * @param method - test method
     * @return array of method parameter values with any AppClientWrapper type set to the active
     * {@link ManagedDeployableContainer#getAppClient()}
     */
    @Override
    public Object[] resolve(Method method) {
        Object[] values = new Object[method.getParameterTypes().length];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if(parameterTypes[i].isAssignableFrom(AppClientWrapper.class)) {
                values[i] = getAppClient();
            }
        }
        return values;
    }

    /**
     * Obtain the AppClientWrapper from the active ManagedDeployableContainer
     * @return active AppClientWrapper if one exists, null otherwise
     */
    private AppClientWrapper getAppClient() {
        String containerID = containerContext.get().getActiveId();
        Container container = containerRegistry.get().getContainer(containerID);
        DeployableContainer<?> deployableContainer = container.getDeployableContainer();
        if(deployableContainer instanceof ManagedDeployableContainer) {
            ManagedDeployableContainer mdContainer = (ManagedDeployableContainer) deployableContainer;
            return mdContainer.getAppClient();
        }
        return null;
    }

    /**
     *
     * @param testCase test case instance
     * @return fields of type assignable from AppClientWrapper
     */
    private List<Field> getAppClientFields(Object testCase) {
        Class<?> testClass = testCase.getClass();
        Class<?> nextTestClass = testClass;
        List<Field> foundFields = new ArrayList<Field>();
        while (nextTestClass != Object.class) {
            for (Field field : testClass.getDeclaredFields()) {
                if (field.getType().isAssignableFrom(AppClientWrapper.class)) {
                    if (!field.canAccess(testCase)) {
                        field.setAccessible(true);
                    }
                    foundFields.add(field);
                }
            }
            nextTestClass = nextTestClass.getSuperclass();
        }

        return foundFields;
    }
}

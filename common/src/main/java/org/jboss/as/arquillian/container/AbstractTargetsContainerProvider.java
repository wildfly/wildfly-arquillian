/*
 * Copyright 2017 Red Hat, Inc.
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

package org.jboss.as.arquillian.container;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.context.ContainerContext;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.container.test.impl.enricher.resource.OperatesOnDeploymentAwareProvider;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;

/**
 * A resource provider which will run in a containers context if the {@link TargetsContainer} annotation is used.
 * <p>
 * Note this overrides the {@link org.jboss.arquillian.container.test.api.OperateOnDeployment}
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public abstract class AbstractTargetsContainerProvider extends OperatesOnDeploymentAwareProvider {
    @Inject
    private Instance<ContainerContext> containerContext;

    @Inject
    private Instance<ContainerRegistry> containerRegistryInstance;

    @Override
    public Object lookup(final ArquillianResource resource, final Annotation... qualifiers) {
        final TargetsContainer targetsContainer = find(qualifiers);
        if (targetsContainer != null) {
            return lookupInContainerContext(targetsContainer, resource, qualifiers);
        }
        return super.lookup(resource, qualifiers);
    }

    /**
     * Looks up the object in the containers context. The container used is provided by the {@link TargetsContainer}
     * annotation.
     *
     * @param targetsContainer the target container
     * @param resource         the resource annotation
     * @param qualifiers       any qualifier annotations
     *
     * @return the object found in the context
     */
    private Object lookupInContainerContext(final TargetsContainer targetsContainer, final ArquillianResource resource,
            final Annotation... qualifiers) {
        final ContainerRegistry registry = containerRegistryInstance.get();
        final ContainerContext context = containerContext.get();
        boolean contextActivated = false;
        try {
            final String name = targetsContainer.value();
            final Container container = registry.getContainer(name);
            if (container == null) {
                throw new IllegalArgumentException(String.format("No container named %s found in the registry.", name));
            }
            context.activate(name);
            contextActivated = true;
            return doLookup(resource, qualifiers);
        } finally {
            if (contextActivated) {
                context.deactivate();
            }
        }
    }

    /**
     * Finds, if present, the {@link TargetsContainer} annotation.
     *
     * @param annotations the annotations to search
     *
     * @return the annotation or {@code null} if the annotation was not present
     */
    private TargetsContainer find(final Annotation... annotations) {
        if (annotations != null) {
            for (Annotation a : annotations) {
                if (a instanceof TargetsContainer) {
                    return TargetsContainer.class.cast(a);
                }
            }
        }
        return null;
    }
}

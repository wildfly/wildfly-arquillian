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
package org.jboss.as.arquillian.container.controller;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.as.arquillian.api.WildFlyContainerController;

/**
 * ResourceProvider for WildFlyContainerController instances for injections running in container.
 *
 * @author Radoslav Husar
 * @version Jan 2015
 */
public class InContainerWildFlyContainerControllerProvider implements ResourceProvider {

    @Inject
    private Instance<WildFlyContainerController> controller;

    /**
     * @see org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider#lookup(org.jboss.arquillian.test.api.ArquillianResource, java.lang.annotation.Annotation...)
     */
    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return controller.get();
    }

    /**
     * @see org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider#canProvide(java.lang.Class)
     */
    @Override
    public boolean canProvide(Class<?> type) {
        return type.isAssignableFrom(WildFlyContainerController.class);
    }
}

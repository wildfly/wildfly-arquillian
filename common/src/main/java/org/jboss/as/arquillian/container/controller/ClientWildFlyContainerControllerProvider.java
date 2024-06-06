/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.controller;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.as.arquillian.api.WildFlyContainerController;

/**
 * ResourceProvider for WildFlyContainerController instances for injections running as client.
 *
 * @author Radoslav Husar
 * @version Jan 2015
 */
public class ClientWildFlyContainerControllerProvider implements ResourceProvider {

    @Inject
    private Instance<WildFlyContainerController> controller;

    /**
     * @see org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider#lookup(org.jboss.arquillian.test.api.ArquillianResource,
     *          java.lang.annotation.Annotation...)
     */
    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return controller.get();
    }

    /**
     * @see org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider#canProvide(Class)
     */
    @Override
    public boolean canProvide(Class<?> type) {
        return type.isAssignableFrom(WildFlyContainerController.class);
    }
}

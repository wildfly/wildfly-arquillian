/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.domain.container.controller;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.wildfly.arquillian.domain.api.DomainContainerController;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class DomainContainerControllerProvider implements ResourceProvider {

    @Inject
    private Instance<DomainContainerController> controller;

    @Override
    public boolean canProvide(Class<?> type) {
        return type.isAssignableFrom(DomainContainerController.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return controller.get();
    }
}

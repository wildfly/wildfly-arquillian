/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.arquillian.testenricher.msc;

import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.as.server.CurrentServiceContainer;
import org.jboss.msc.service.ServiceContainer;

/**
 * {@link ResourceProvider} implementation to
 * provide {@link ServiceContainer} injection to {@link ArquillianResource}-
 * annotated fields.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 16-May-2013
 */
public class ServiceContainerProvider implements ResourceProvider {

    private AtomicBoolean initialized = new AtomicBoolean();

    @Inject
    @SuiteScoped
    private InstanceProducer<ServiceContainer> serviceContainerProducer;

    @Inject
    private Instance<ServiceContainer> serviceContainer;

    @Override
    public boolean canProvide(final Class<?> type) {
        return type.isAssignableFrom(ServiceContainer.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        return AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                if (initialized.compareAndSet(false, true)) {
                    ServiceContainer serviceContainer = CurrentServiceContainer.getServiceContainer();
                    serviceContainerProducer.set(serviceContainer);
                }
                return serviceContainer.get();
            }
        });
    }
}

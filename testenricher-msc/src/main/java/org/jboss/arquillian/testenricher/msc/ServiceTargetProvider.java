/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.arquillian.testenricher.msc;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.msc.service.ServiceTarget;

/**
 * {@link OperatesOnDeploymentAwareProvider} implementation to
 * provide {@link ServiceTarget} injection to {@link ArquillianResource}-
 * annotated fields.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 16-May-2013
 */
public class ServiceTargetProvider implements ResourceProvider {

    @Inject
    @ClassScoped
    private InstanceProducer<ServiceTarget> serviceTargetProducer;

    @Inject
    private Instance<ServiceTarget> serviceTarget;

    @Inject
    @ClassInjection
    private Class testClass;

    @Override
    public boolean canProvide(final Class<?> type) {
        return type.isAssignableFrom(ServiceTarget.class);
    }

    @Override
    public Object lookup(ArquillianResource resource, Annotation... qualifiers) {
        ServiceTarget serviceTarget = ServiceTargetAssociation.getServiceTarget(testClass.getName());
        serviceTargetProducer.set(serviceTarget);
        return serviceTarget;
    }
}

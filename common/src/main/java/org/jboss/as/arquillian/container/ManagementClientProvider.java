/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.container.test.impl.enricher.resource.OperatesOnDeploymentAwareProvider;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.test.api.ArquillianResource;

/**
 * {@link OperatesOnDeploymentAwareProvider} implementation to
 * provide {@link ManagementClient} injection to {@link ArquillianResource}-
 * annotated fields.
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class ManagementClientProvider extends AbstractTargetsContainerProvider {

    @Inject
    private Instance<ManagementClient> managementClient;

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider#canProvide(java.lang.Class)
     */
    @Override
    public boolean canProvide(final Class<?> type) {
        return type.isAssignableFrom(ManagementClient.class);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.arquillian.container.test.impl.enricher.resource.OperatesOnDeploymentAwareProvider#doLookup(org.jboss.arquillian.test.api.ArquillianResource,
     *          java.lang.annotation.Annotation[])
     */
    @Override
    public Object doLookup(final ArquillianResource resource, final Annotation... qualifiers) {
        return managementClient.get();
    }

}

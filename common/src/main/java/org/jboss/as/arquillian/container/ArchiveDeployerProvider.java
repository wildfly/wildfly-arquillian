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
 * provide {@link ArchiveDeployer} injection to {@link ArquillianResource}-
 * annotated fields.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ArchiveDeployerProvider extends AbstractTargetsContainerProvider {

    @Inject
    private Instance<ArchiveDeployer> deployer;

    /**
     * {@inheritDoc}
     *
     * @see org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider#canProvide(Class)
     */
    @Override
    public boolean canProvide(final Class<?> type) {
        return type.isAssignableFrom(ArchiveDeployer.class);
    }

    /**
     * {@inheritDoc}
     *
     * @see OperatesOnDeploymentAwareProvider#doLookup(ArquillianResource, Annotation[])
     */
    @Override
    public Object doLookup(final ArquillianResource resource, final Annotation... qualifiers) {
        return deployer.get();
    }

}

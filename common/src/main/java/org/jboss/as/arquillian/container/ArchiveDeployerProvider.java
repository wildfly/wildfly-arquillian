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

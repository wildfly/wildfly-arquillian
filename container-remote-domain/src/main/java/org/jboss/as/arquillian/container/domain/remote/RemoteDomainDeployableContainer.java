/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.domain.remote;

import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.as.arquillian.container.domain.CommonDomainDeployableContainer;

/**
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class RemoteDomainDeployableContainer extends CommonDomainDeployableContainer<RemoteDomainContainerConfiguration> {

    @Override
    public Class<RemoteDomainContainerConfiguration> getConfigurationClass() {
        return RemoteDomainContainerConfiguration.class;
    }

    @Override
    protected void startInternal() throws LifecycleException {
        // no-op
    }

    @Override
    protected void stopInternal() throws LifecycleException {
        // no-op
    }
}

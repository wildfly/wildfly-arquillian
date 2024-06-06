/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.remote;

import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.as.arquillian.container.CommonDeployableContainer;

/**
 * JBossASRemoteContainer
 *
 * @author Thomas.Diesler@jboss.com
 * @since 17-Nov-2010
 */
public final class RemoteDeployableContainer extends
        CommonDeployableContainer<RemoteContainerConfiguration> {

    @Override
    protected void startInternal() throws LifecycleException {
    }

    @Override
    protected void stopInternal(Integer timeout) throws LifecycleException {
        // Do nothing.
    }

    @Override
    public Class<RemoteContainerConfiguration> getConfigurationClass() {
        return RemoteContainerConfiguration.class;
    }
}
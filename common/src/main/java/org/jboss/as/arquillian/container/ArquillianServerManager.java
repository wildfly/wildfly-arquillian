/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.wildfly.plugin.tools.ContainerDescription;
import org.wildfly.plugin.tools.DeploymentManager;
import org.wildfly.plugin.tools.server.ServerManager;

/**
 * A delegating implementation of a {@link ServerManager} which does not allow {@link #shutdown()} attempts. If either
 * shutdown method is invoked, an {@link UnsupportedOperationException} will be thrown.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class ArquillianServerManager implements ServerManager {

    private final ServerManager delegate;

    ArquillianServerManager(ServerManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public ModelControllerClient client() {
        return delegate.client();
    }

    @Override
    public String serverState() {
        return delegate.serverState();
    }

    @Override
    public String launchType() {
        return delegate.launchType();
    }

    @Override
    public String takeSnapshot() throws IOException {
        return delegate.takeSnapshot();
    }

    @Override
    public ContainerDescription containerDescription() throws IOException {
        return delegate.containerDescription();
    }

    @Override
    public DeploymentManager deploymentManager() {
        return delegate.deploymentManager();
    }

    @Override
    public boolean isRunning() {
        return delegate.isRunning();
    }

    @Override
    public boolean waitFor(final long startupTimeout) throws InterruptedException {
        return delegate.waitFor(startupTimeout);
    }

    @Override
    public boolean waitFor(final long startupTimeout, final TimeUnit unit) throws InterruptedException {
        return delegate.waitFor(startupTimeout, unit);
    }

    /**
     * Throws an {@link UnsupportedOperationException} as the server is managed by Arquillian
     *
     * @throws UnsupportedOperationException as the server is managed by Arquillian
     */
    @Override
    public void shutdown() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot shutdown a server managed by Arquillian");
    }

    /**
     * Throws an {@link UnsupportedOperationException} as the server is managed by Arquillian
     *
     * @throws UnsupportedOperationException s the server is managed by Arquillian
     */
    @Override
    public void shutdown(final long timeout) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot shutdown a server managed by Arquillian");
    }

    @Override
    public void executeReload() throws IOException {
        delegate.executeReload();
    }

    @Override
    public void executeReload(final ModelNode reloadOp) throws IOException {
        delegate.executeReload(reloadOp);
    }

    @Override
    public void reloadIfRequired() throws IOException {
        delegate.reloadIfRequired();
    }

    @Override
    public void reloadIfRequired(final long timeout, final TimeUnit unit) throws IOException {
        delegate.reloadIfRequired(timeout, unit);
    }
}

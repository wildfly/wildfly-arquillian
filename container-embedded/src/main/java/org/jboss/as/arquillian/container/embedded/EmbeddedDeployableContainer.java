/*
 * Copyright 2015 Red Hat, Inc.
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
package org.jboss.as.arquillian.container.embedded;

import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.as.arquillian.container.CommonDeployableContainer;
import org.wildfly.core.embedded.EmbeddedProcessFactory;
import org.wildfly.core.embedded.EmbeddedStandaloneServerFactory;
import org.wildfly.core.embedded.StandaloneServer;

/**
 * {@link org.jboss.arquillian.container.spi.client.container.DeployableContainer} implementation to bootstrap JBoss Logging (installing the LogManager if possible), use the JBoss
 * Modules modular ClassLoading Environment to create a new server instance, and handle lifecycle of the Application Server in
 * the currently-running environment.
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 * @author <a href="mailto:mmatloka@gmail.com">Michal Matloka</a>
 * @author Thomas.Diesler@jboss.com
 */
public final class EmbeddedDeployableContainer extends CommonDeployableContainer<EmbeddedContainerConfiguration> {

    /**
     * Hook to the server; used in start/stop, created by setup
     */
    private StandaloneServer server;

    @Override
    public void setup(final EmbeddedContainerConfiguration config) {
        super.setup(config);
        if (config.getCleanServerBaseDir() != null) {
            SecurityActions.setSystemProperty(EmbeddedStandaloneServerFactory.JBOSS_EMBEDDED_ROOT, config.getCleanServerBaseDir());
        }
        server = EmbeddedProcessFactory.createStandaloneServer(config.getJbossHome(), config.getModulePath());
    }

    @Override
    public Class<EmbeddedContainerConfiguration> getConfigurationClass() {
        return EmbeddedContainerConfiguration.class;
    }

    @Override
    protected void startInternal() throws LifecycleException {
        try {
            server.start();
        } catch (Throwable e) {
            throw new LifecycleException("Could not invoke start on: " + server, e);
        }
    }

    @Override
    protected void stopInternal(Integer timeout) throws LifecycleException {
        try {
            // Timeout is ignored in the embeddable case.
            server.stop();
        } catch (Throwable e) {
            throw new LifecycleException("Could not invoke stop on: " + server, e);
        }
    }
}

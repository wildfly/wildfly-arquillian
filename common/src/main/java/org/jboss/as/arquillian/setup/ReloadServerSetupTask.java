/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.arquillian.setup;

import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.wildfly.plugin.tools.server.ServerManager;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings({ "unused", "RedundantThrows" })
public class ReloadServerSetupTask implements ServerSetupTask {

    @ArquillianResource
    private ServerManager serverManager;

    @Override
    public final void setup(final ManagementClient managementClient, final String containerId) throws Exception {
        try {
            doSetup(managementClient, containerId);
        } finally {
            serverManager.reloadIfRequired();
        }
    }

    @Override
    public final void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
        try {
            doTearDown(managementClient, containerId);
        } finally {
            serverManager.reloadIfRequired();
        }
    }

    /**
     * Execute any necessary setup work that needs to happen before the first deployment to the given container.
     *
     * @param client      management client to use to interact with the container
     * @param containerId id of the container to which the deployment will be deployed
     *
     * @throws Exception if a failure occurs
     * @see #setup(ManagementClient, String)
     */
    protected void doSetup(final ManagementClient client, final String containerId) throws Exception {
    }

    /**
     * Execute any tear down work that needs to happen after the last deployment associated
     * with the given container has been undeployed.
     *
     * @param managementClient management client to use to interact with the container
     * @param containerId      id of the container to which the deployment will be deployed
     *
     * @throws Exception if a failure occurs
     * @see #tearDown(ManagementClient, String)
     */
    protected void doTearDown(final ManagementClient managementClient, final String containerId) throws Exception {
    }
}

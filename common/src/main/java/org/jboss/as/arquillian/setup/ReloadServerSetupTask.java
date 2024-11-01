/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.setup;

import org.jboss.as.arquillian.api.ReloadIfRequired;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;

/**
 * A {@link ServerSetupTask} which will reload the server, if required, after the {@link #setup(ManagementClient, String)}
 * and {@link #tearDown(ManagementClient, String)} methods are invoked.
 * <p>
 * This can be used as the last {@link ServerSetupTask} in the chain to ensure the server is in the correct state after
 * the other setup tasks have executed.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @since 5.1
 */
@SuppressWarnings({ "unused", "RedundantThrows" })
@ReloadIfRequired
public class ReloadServerSetupTask implements ServerSetupTask {

    @Override
    public void setup(final ManagementClient managementClient, final String containerId) throws Exception {
    }

    @Override
    public void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
    }
}

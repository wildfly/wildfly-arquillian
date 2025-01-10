/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed.manual;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ArchiveDeployer;
import org.jboss.as.arquillian.container.ManagementClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests that the injected management client and archive deployer are usable on the appropriate container.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
@Tag("ManualMode")
public class ClientManualModeTestCase extends AbstractManualModeTestCase {
    private static final String CONTAINER_NAME = "jboss";

    @ArquillianResource
    @TargetsContainer(CONTAINER_NAME)
    private ManagementClient client;

    @ArquillianResource
    @TargetsContainer(CONTAINER_NAME)
    private ArchiveDeployer deployer;

    @Override
    protected String containerName() {
        return CONTAINER_NAME;
    }

    @Override
    protected ManagementClient client() {
        return client;
    }

    @Override
    protected ArchiveDeployer deployer() {
        return deployer;
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed.manual;

import java.util.Map;

import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ArchiveDeployer;
import org.jboss.as.arquillian.container.ManagementClient;
import org.junit.Ignore;
import org.junit.runner.RunWith;

/**
 * Tests that
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Arquillian.class)
public class InContainerManualModeTestCase extends AbstractManualModeTestCase {
    private static final String CONTAINER_NAME = "wildfly";

    @ArquillianResource
    @TargetsContainer(CONTAINER_NAME)
    private ManagementClient client;

    @Override
    protected Map<String, String> createConfig(final String suffix) {
        return Map.of("cleanServerBaseDir", System.getProperty("java.io.tmpdir") + "/wildfly-" + suffix);
    }

    @Override
    @Ignore("The ArchiveDeployer does not work because of missing dependencies. This should be resolved with WFARQ-148.")
    public void deploy() throws Exception {
    }

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
        return new ArchiveDeployer(client);
    }
}

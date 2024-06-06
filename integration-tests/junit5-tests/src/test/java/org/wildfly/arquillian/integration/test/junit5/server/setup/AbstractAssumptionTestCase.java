/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.junit5.server.setup;

import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ServerSetup(AbstractAssumptionTestCase.AssumingServerSetupTask.class)
abstract class AbstractAssumptionTestCase {

    public static class AssumingServerSetupTask implements ServerSetupTask {

        @Override
        public void setup(final ManagementClient managementClient, final String containerId) throws Exception {
            Assumptions.abort("Abort on purpose");
        }

        @Override
        public void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {

        }
    }

    @Test
    public void failIfExecuted() {
        Assertions.fail("This should have been skipped and not executed");
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Function;

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

abstract class ServerSetupAssumptionTestBase {

    /**
     * Creates a deployment that always fails to deploy. If the server setup task fails to disable
     * further work on a test class that uses this, this will get deployed and fail.
     *
     * @param name the deployment archive file name
     *
     * @return a deployment that will fail to deploy
     */
    protected static JavaArchive createDeployment(String name) {
        return ShrinkWrap.create(JavaArchive.class, name)
                .addClasses(FailedDeployEjbBean.class, EjbBusiness.class);
    }

    @Test
    public void test() {
        fail("Test was not skipped");
    }

    static class AroundSetup implements ServerSetupTask {

        private final String property;

        AroundSetup(String property) {
            this.property = property;
        }

        @Override
        public void setup(ManagementClient managementClient, String containerId) {
            System.setProperty(property, "setup");
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) {
            System.setProperty(property, "tearDown");
        }
    }

    static class AssumptionViolatedSetup implements ServerSetupTask {

        private final String property;
        private final Function<String, RuntimeException> assumptionFailureProducer;

        AssumptionViolatedSetup(String property) {
            this(property, TestAbortedException::new);
        }

        AssumptionViolatedSetup(String property, Function<String, RuntimeException> assumptionFailureProducer) {
            this.property = property;
            this.assumptionFailureProducer = assumptionFailureProducer;
        }

        @Override
        public void setup(ManagementClient managementClient, String containerId) {
            System.setProperty(property, "setup");
            throw assumptionFailureProducer.apply("always");
        }

        @Override
        public void tearDown(ManagementClient managementClient, String containerId) {
            System.setProperty(property, "tearDown");
            throw assumptionFailureProducer.apply("always");
        }
    }
}

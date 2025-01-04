/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * In conjunction with {@link ServerSetupAfterClassTestCase#testAssumptionViolated()}, tests
 * what happens when a {@link ServerSetupTask} throws {@link org.opentest4j.TestAbortedException}
 * when it executes prior to Arquillian deploying a {@code @Deployment(managed=false)} deployment.
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
@ServerSetup({
        ServerSetupUnmanagedAssumptionViolationTestCase.BeforeSetup.class,
        ServerSetupUnmanagedAssumptionViolationTestCase.AssumptionViolatedSetup.class,
        ServerSetupUnmanagedAssumptionViolationTestCase.AfterSetup.class
})
public class ServerSetupUnmanagedAssumptionViolationTestCase extends ServerSetupAssumptionTestBase {

    private static final String DEPLOYMENT = "ServerSetupUnmanagedAssumptionViolationTestCase.jar";

    /**
     * A deployment that always fails to deploy. If the server setup task fails to disable
     * further work on this test class, this will get deployed and fail.
     *
     * @return a deployment that will fail to deploy
     */
    @Deployment(managed = false, name = DEPLOYMENT)
    public static JavaArchive createDeployment() {
        return createDeployment(DEPLOYMENT);
    }

    @ArquillianResource
    private Deployer deployer;

    @Override
    @Test
    public void test() {
        // Deploying should trigger the ServerSetupTasks and those should stop further processing
        deployer.deploy(DEPLOYMENT);

        super.test();
    }

    public static class BeforeSetup extends ServerSetupAssumptionTestBase.AroundSetup {

        public static final String PROPERTY = BeforeSetup.class.getName();

        public BeforeSetup() {
            super(PROPERTY);
        }
    }

    public static class AssumptionViolatedSetup extends ServerSetupAssumptionTestBase.AssumptionViolatedSetup {
        public static final String PROPERTY = AssumptionViolatedSetup.class.getName();

        public AssumptionViolatedSetup() {
            super(PROPERTY);
        }
    }

    public static class AfterSetup extends ServerSetupAssumptionTestBase.AroundSetup {

        public static final String PROPERTY = AfterSetup.class.getName();

        public AfterSetup() {
            super(PROPERTY);
        }
    }
}

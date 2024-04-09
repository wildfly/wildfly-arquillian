/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AssumptionViolatedException;
import org.junit.runner.RunWith;

/**
 * In conjunction with {@link ServerSetupAfterClassTestCase#testAssumptionViolated()}, tests
 * what happens when a {@link ServerSetupTask} throws {@link AssumptionViolatedException}
 * when it executes prior to Arquillian deploying a {@code @Deployment(managed=true)} deployment.
 */
@RunWith(Arquillian.class)
@RunAsClient
@ServerSetup({
        ServerSetupAssumptionViolationTestCase.BeforeSetup.class,
        ServerSetupAssumptionViolationTestCase.AssumptionViolatedSetup.class,
        ServerSetupAssumptionViolationTestCase.AfterSetup.class
})
public class ServerSetupAssumptionViolationTestCase extends ServerSetupAssumptionTestBase {

    private static final String DEPLOYMENT = "ServerSetupAssumptionViolationTestCase.jar";

    /**
     * A deployment that always fails to deploy. If the server setup task fails to disable
     * further work on this test class, this will get deployed and fail.
     *
     * @return a deployment that will fail to deploy
     */
    @Deployment(name = DEPLOYMENT)
    public static JavaArchive createDeployment() {
        return createDeployment(DEPLOYMENT);
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

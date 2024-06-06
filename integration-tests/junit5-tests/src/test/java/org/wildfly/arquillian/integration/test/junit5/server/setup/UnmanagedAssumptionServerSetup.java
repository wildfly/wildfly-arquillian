/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.junit5.server.setup;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.arquillian.integration.test.junit5.GreeterServlet;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("NewClassNamingConvention")
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class UnmanagedAssumptionServerSetup extends AbstractAssumptionTestCase {

    @ArquillianResource
    private Deployer deployer;

    @Deployment(managed = false, name = "unmanaged")
    public static WebArchive create() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(GreeterServlet.class);
    }

    @Override
    @Test
    public void failIfExecuted() {
        // Until ARQ-2231 this must happen in the test context
        deployer.deploy("unmanaged");
        super.failIfExecuted();
    }
}

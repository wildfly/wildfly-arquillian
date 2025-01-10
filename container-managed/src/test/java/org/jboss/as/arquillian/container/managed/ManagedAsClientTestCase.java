/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import java.io.File;
import java.net.URL;

import javax.management.MBeanServerConnection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.as.arquillian.container.MBeanServerConnectionProvider;
import org.jboss.as.arquillian.container.managed.archive.ConfigService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xnio.IoUtils;

/**
 * JBossASRemoteIntegrationTestCase
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author Thomas.Diesler@jboss.com
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class ManagedAsClientTestCase extends AbstractContainerTestCase {
    private MBeanServerConnectionProvider provider;

    @Deployment(testable = false)
    public static JavaArchive createDeployment() throws Exception {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "sar-example.sar");
        archive.addPackage(ConfigService.class.getPackage());
        String path = "META-INF/jboss-service.xml";
        URL resourceURL = ManagedAsClientTestCase.class.getResource("/sar-example.sar/" + path);
        archive.addAsResource(new File(resourceURL.getFile()), path);
        return archive;
    }

    @Override
    protected MBeanServerConnection getMBeanServer() throws Exception {
        assert provider == null;
        provider = MBeanServerConnectionProvider.defaultProvider();
        return provider.getConnection();
    }

    @AfterEach
    public void closeProvider() {
        IoUtils.safeClose(provider);
    }

    @Override
    @Test
    public void testDeployedService() throws Exception {
        super.testDeployedService();
    }
}

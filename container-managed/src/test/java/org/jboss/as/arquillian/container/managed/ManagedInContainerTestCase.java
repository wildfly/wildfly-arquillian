/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLDecoder;

import javax.management.MBeanServerConnection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.container.managed.archive.ConfigService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.runner.RunWith;

/**
 * ManagedInContainerTestCase
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author Thomas.Diesler@jboss.com
 */
@RunWith(Arquillian.class)
public class ManagedInContainerTestCase extends AbstractContainerTestCase {

    @Deployment
    public static JavaArchive createDeployment() throws Exception {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "sar-example.sar");
        archive.addPackage(ConfigService.class.getPackage());
        archive.addClasses(AbstractContainerTestCase.class);
        String path = "META-INF/jboss-service.xml";
        URL resourceURL = ManagedInContainerTestCase.class.getResource("/sar-example.sar/" + path);
        String resourceFilePath = URLDecoder.decode(resourceURL.getFile(), "UTF-8");
        archive.addAsResource(new File(resourceFilePath), path);
        return archive;
    }

    @Override
    MBeanServerConnection getMBeanServer() throws Exception {
        return ManagementFactory.getPlatformMBeanServer();
    }

    @Override
    public void testDeployedService() throws Exception {
        super.testDeployedService();
    }
}

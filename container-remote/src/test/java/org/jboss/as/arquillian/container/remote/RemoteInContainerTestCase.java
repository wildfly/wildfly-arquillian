/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.remote;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;

import javax.management.MBeanServerConnection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.as.arquillian.container.remote.archive.ConfigService;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * ManagedInContainerTestCase
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author Thomas.Diesler@jboss.com
 */
@ExtendWith(ArquillianExtension.class)
public class RemoteInContainerTestCase extends AbstractContainerTestCase {

    @Deployment
    public static JavaArchive createDeployment() throws Exception {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "sar-example.sar");
        archive.addPackage(ConfigService.class.getPackage());
        archive.addClasses(AbstractContainerTestCase.class);
        String path = "META-INF/jboss-service.xml";
        URL resourceURL = RemoteInContainerTestCase.class.getResource("/sar-example.sar/" + path);
        archive.addAsResource(new File(resourceURL.getFile()), path);
        return archive;
    }

    @Override
    MBeanServerConnection getMBeanServer() throws Exception {
        return ManagementFactory.getPlatformMBeanServer();
    }
}

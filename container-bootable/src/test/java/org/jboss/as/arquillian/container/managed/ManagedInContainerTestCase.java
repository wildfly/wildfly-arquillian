/*
 * Copyright 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.junit.Ignore;
import org.junit.runner.RunWith;

/**
 * ManagedInContainerTestCase
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author Thomas.Diesler@jboss.com
 */
// Ignored, requires sar not present in standalone-microprofile.xml
@Ignore
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

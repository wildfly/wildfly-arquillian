/*
 * Copyright 2023 Red Hat, Inc.
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
package org.jboss.as.arquillian.container.app;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.container.managed.AppClientWrapper;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * Test deployment of an application client ear and application using the extended managed container.
 * This version automatically starts the application client container after starting the server and deploying
 * the test EAR.
 *
 * To run in an IDE, set the -Darquillian.xml=arqullian-appclient.xml property the test VM arguments
 */
@RunWith(Arquillian.class)
public class AppClientTestCase {

    /**
     * Test an application client accessing an EJB in the managed server
     */
    @Deployment(testable = false)
    public static EnterpriseArchive createDeployment() throws Exception {
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "appClient" + ".ear");

        JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "myejb.jar")
                .addClasses(EjbBean.class, EjbBusiness.class);
        ear.addAsModule(ejbJar);

        final JavaArchive appClient = ShrinkWrap.create(JavaArchive.class, "client-annotation.jar");
        appClient.addClasses(AppClientMain.class);
        appClient.addAsManifestResource(new StringAsset("Main-Class: " + AppClientMain.class.getName() + "\n"), "MANIFEST.MF");
        ear.addAsModule(appClient);

        File archiveOnDisk = new File("target" + File.separator + ear.getName());
        if (archiveOnDisk.exists()) {
            archiveOnDisk.delete();
        }
        final ZipExporter exporter = ear.as(ZipExporter.class);
        exporter.exportTo(archiveOnDisk);
        String archivePath = archiveOnDisk.getAbsolutePath();
        System.out.printf("archivePath: %s\n", archivePath);

        return ear;
    }

    /**
     * Test using JBossModulesCommandBuilder
     * @throws Exception
     */
    @Test
    @RunAsClient
    public void testAppClientRunViaArq(AppClientWrapper appClient) throws Exception {
        String[] output = appClient.readAll(1000);
        System.out.printf("AppClient readAll returned %d lines\n", output.length);
        boolean sawStart = false, sawEnd = false, sawResult = false, sawSuccess = false, sawFailed = false;
        for(String line : output) {
            System.out.println(line);
            if(line.contains("AppClientMain.begin")) {
                sawStart = true;
            } else if(line.contains("AppClientMain.end")) {
                sawEnd = true;
            } else if(line.contains("AppClientMain.FAILED")) {
                sawFailed = true;
            } else if(line.contains("AppClientMain.SUCCESS")) {
                sawSuccess = true;
            } else if(line.contains("AppClientMain.RESULT: clientCall(testAppClientRunViaArq)")) {
                sawResult = true;
            }
        }
        Assert.assertTrue("AppClientMain.begin was seen", sawStart);
        Assert.assertTrue("AppClientMain.end was seen", sawEnd);
        Assert.assertFalse("AppClientMain.FAILED was seen", sawFailed);
        Assert.assertTrue("AppClientMain.SUCCESS was seen", sawSuccess);
        Assert.assertTrue("AppClientMain.RESULT with testAppClientRunViaArq was seen", sawResult);
    }

}

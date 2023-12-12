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

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.managed.AppClientWrapper;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test deployment of an application client ear and application using the extended managed container.
 * This version only automatically starts the server and deploys the test EAR. The {@link #testAppClientRun()}
 * (AppClientWrapper) }
 * method explicitly starts the application client with the injected AppClientWrapper and then validates its
 * output.
 * <p>
 * To run in an IDE, set the -Darquillian.xml=appclient-arqullian.xml -Darqullian.launch=jboss-manual-client
 * properties the test VM arguments
 * </p>
 */
@RunWith(Arquillian.class)
public class AppClient2TestCase {

    /**
     * Test an application client accessing an EJB in the managed server
     */
    @TargetsContainer("jboss-manual-client")
    @Deployment(testable = false, name = "jboss-manual-client")
    public static EnterpriseArchive createDeployment() throws Exception {
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "appClient.ear");

        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "myejb.jar")
                .addClasses(EjbBean.class, EjbBusiness.class);
        ear.addAsModule(ejbJar);

        final JavaArchive appClient = ShrinkWrap.create(JavaArchive.class, "client-annotation.jar");
        appClient.addClasses(AppClientMain.class);
        appClient.addAsManifestResource(new StringAsset("Main-Class: " + AppClientMain.class.getName() + "\n"), "MANIFEST.MF");
        ear.addAsModule(appClient);

        final File archiveOnDisk = new File("target" + File.separator + ear.getName());
        final ZipExporter exporter = ear.as(ZipExporter.class);
        exporter.exportTo(archiveOnDisk, true);

        return ear;
    }

    @ArquillianResource
    private AppClientWrapper appClient;

    /**
     * Launch the EE Application client container using the same EAR to validate access to the deployed EJB
     * using the injected AppClientWrapper.
     */
    @Test
    @RunAsClient
    @TargetsContainer("jboss-manual-client")
    public void testAppClientRun() throws Exception {
        // Launch the application client container
        appClient.run();
        appClient.waitForExit(10, TimeUnit.SECONDS);
        System.out.println("AppClient exited");

        List<String> output = appClient.readAll(1000);
        System.out.printf("AppClient readAll returned %d lines%n", output.size());
        boolean sawStart = false, sawEnd = false, sawResult = false, sawSuccess = false, sawFailed = false;
        for (String line : output) {
            System.out.println(line);
            if (line.contains("AppClientMain.begin")) {
                sawStart = true;
            } else if (line.contains("AppClientMain.end")) {
                sawEnd = true;
            } else if (line.contains("AppClientMain.FAILED")) {
                sawFailed = true;
            } else if (line.contains("AppClientMain.SUCCESS")) {
                sawSuccess = true;
            } else if (line.contains("AppClientMain.RESULT: clientCall(testAppClientRun)")) {
                sawResult = true;
            }
        }
        // Cleanup the app client
        appClient.close();

        Assert.assertTrue("AppClientMain.begin was seen", sawStart);
        Assert.assertTrue("AppClientMain.end was seen", sawEnd);
        Assert.assertFalse("AppClientMain.FAILED was seen", sawFailed);
        Assert.assertTrue("AppClientMain.SUCCESS was seen", sawSuccess);
        Assert.assertTrue("AppClientMain.RESULT with testAppClientRun was seen", sawResult);
    }

}

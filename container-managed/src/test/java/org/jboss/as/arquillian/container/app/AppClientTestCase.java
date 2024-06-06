/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.app;

import java.io.File;
import java.util.List;

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
 * This version automatically starts the application client container after starting the server and deploying
 * the test EAR.
 * <p>
 * To run in an IDE, set the -Darquillian.xml=appclient-arqullian.xml property the test VM arguments
 * </p>
 */
@RunWith(Arquillian.class)
public class AppClientTestCase {

    /**
     * Test an application client accessing an EJB in the managed server
     */
    @TargetsContainer("jboss")
    @Deployment(testable = false, name = "jboss")
    public static EnterpriseArchive createDeployment() throws Exception {
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "appClient" + ".ear");

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

    /**
     * Test using JBossModulesCommandBuilder
     *
     */
    @Test
    @RunAsClient
    public void testAppClientRunViaArq(@ArquillianResource AppClientWrapper appClient) {
        final List<String> output = appClient.readAll(1000);
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
            } else if (line.contains("AppClientMain.RESULT: clientCall(testAppClientRunViaArq)")) {
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

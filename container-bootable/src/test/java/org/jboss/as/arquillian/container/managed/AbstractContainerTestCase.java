/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import javax.management.Attribute;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.junit.Test;

/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author Thomas.Diesler@jboss.com
 */
public abstract class AbstractContainerTestCase {

    @Test
    public void testDeployedService() throws Exception {
        MBeanServerConnection mbeanServer = getMBeanServer();
        ObjectName objectName = new ObjectName("jboss:name=test,type=config");

        // FIXME should have some notification happening when the deployment has been installed for client
        waitForMbean(mbeanServer, objectName);

        mbeanServer.getAttribute(objectName, "IntervalSeconds");
        mbeanServer.setAttribute(objectName, new Attribute("IntervalSeconds", 2));
    }

    abstract MBeanServerConnection getMBeanServer() throws Exception;

    void waitForMbean(MBeanServerConnection mbeanServer, ObjectName name) throws Exception {
        // FIXME remove this
        long end = System.currentTimeMillis() + 3000;
        do {
            try {
                MBeanInfo info = mbeanServer.getMBeanInfo(name);
                if (info != null) {
                    return;
                }
            } catch (Exception e) {
            }
            Thread.sleep(100);
        } while (System.currentTimeMillis() < end);
    }
}

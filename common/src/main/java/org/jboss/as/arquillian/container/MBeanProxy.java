/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * A simple MBeanProxy
 *
 * @author Thomas.Diesler@jboss.com
 * @since 24-Feb-2009
 */
public class MBeanProxy {

    public static <T> T get(MBeanServerConnection server, String name, Class<T> interf) {
        ObjectName oname;
        try {
            oname = ObjectName.getInstance(name);
        } catch (MalformedObjectNameException ex) {
            throw new IllegalArgumentException("Invalid object name: " + name);
        }
        return (T) MBeanProxy.get(server, oname, interf);
    }

    public static <T> T get(MBeanServerConnection server, ObjectName name, Class<T> interf) {
        return (T) MBeanServerInvocationHandler.newProxyInstance(server, name, interf, false);
    }
}

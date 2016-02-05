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

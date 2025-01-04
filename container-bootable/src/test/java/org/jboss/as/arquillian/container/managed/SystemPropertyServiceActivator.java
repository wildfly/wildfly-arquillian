/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;

/**
 * @author Stuart Douglas
 */
public class SystemPropertyServiceActivator implements ServiceActivator {

    public static final String TEST_PROPERTY = "test-property";
    public static final String VALUE = "set";

    @Override
    public void activate(ServiceActivatorContext serviceActivatorContext) throws ServiceRegistryException {
        serviceActivatorContext.getServiceTarget().addService().setInstance(new org.jboss.msc.Service() {
            @Override
            public void start(final StartContext context) {
                System.setProperty(TEST_PROPERTY, VALUE);
            }

            @Override
            public void stop(final StopContext context) {
                System.setProperty(TEST_PROPERTY, VALUE);
            }
        }).install();
    }
}

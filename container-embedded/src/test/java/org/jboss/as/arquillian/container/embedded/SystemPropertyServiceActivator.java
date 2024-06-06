/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.embedded;

import org.jboss.msc.Service;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;

/**
 * @author Stuart Douglas
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class SystemPropertyServiceActivator implements ServiceActivator {

    static final String TEST_PROPERTY = "test-property";
    static final String VALUE = "set";

    @Override
    public void activate(final ServiceActivatorContext serviceActivatorContext) throws ServiceRegistryException {
        serviceActivatorContext.getServiceTarget().addService(ServiceName.of("test-service")).setInstance(new Service() {
            @Override
            public void start(final StartContext context) {
                System.setProperty(TEST_PROPERTY, VALUE);
            }

            @Override
            public void stop(final StopContext context) {
                System.clearProperty(TEST_PROPERTY);
            }
        }).install();
    }

}

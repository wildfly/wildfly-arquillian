/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.service;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;

/**
 * An activator for the {@link ArquillianService}
 *
 * @author Thomas.Diesler@jboss.com
 * @since 16-May-2011
 */
public class ArquillianServiceActivator implements ServiceActivator {

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        ServiceTarget serviceTarget = context.getServiceTarget();
        ArquillianService.addService(serviceTarget);
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.arquillian.testenricher.msc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.msc.service.ServiceTarget;

/**
 * A thread local {@link ServiceTarget} association
 *
 * @author thomas.diesler@jboss.com
 * @author Stuart Douglas
 * @since 18-Nov-2010
 */
public final class ServiceTargetAssociation {

    private static ConcurrentMap<String, ServiceTarget> association = new ConcurrentHashMap<>();

    public static ServiceTarget getServiceTarget(final String className) {
        return association.get(className);
    }

    public static void setServiceTarget(final String className, ServiceTarget type) {
        association.put(className, type);
    }

    public static void clearServiceTarget(final String className) {
        association.remove(className);
    }
}

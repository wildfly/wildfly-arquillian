/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.app;

import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;

/**
 * Test EJB used for binding only
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
@Stateless
@Remote(EjbBusiness.class)
public class EjbBean implements EjbBusiness {
    @Override
    public String clientCall(String args) {
        return String.format("clientCall(%s)", args);
    }
}

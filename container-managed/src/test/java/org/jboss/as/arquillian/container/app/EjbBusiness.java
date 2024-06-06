/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.app;

import jakarta.ejb.Remote;

/**
 * EJB Business Interface
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
@Remote
public interface EjbBusiness {
    public String clientCall(String args);
}

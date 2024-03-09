/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import javax.annotation.PostConstruct;
import javax.ejb.Remote;
import javax.ejb.Stateless;

/**
 * An EJB that always fails in @PostConstruct. This bean is meant for use in that confirm
 * that deployment by Arquillian of a managed deployment is disabled; this bean exists
 * to trigger visible failure if the deployment happens.
 */
@Stateless
@Remote(EjbBusiness.class)
public class FailedDeployEjbBean implements EjbBusiness {

    @PostConstruct
    public void postConstruct() {
        throw new UnsupportedOperationException("The deployment containing this bean should not have been deployed");
    }
}

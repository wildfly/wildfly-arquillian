/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import javax.naming.Context;
import javax.naming.NamingException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * AS7-3111 Ensures the JNDI Naming {@link Context} can be injected
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class InjectJndiContextTestCase {

    /**
     * Test EJB deployment w/ remote binding
     */
    @Deployment(testable = false)
    public static JavaArchive createDeployment() throws Exception {
        return ShrinkWrap.create(JavaArchive.class, "myejb.jar").addClasses(EjbBean.class, EjbBusiness.class);
    }

    /**
     * {@link Context} to be injected
     */
    @ArquillianResource
    private Context jndiContext;

    private static final String JNDI_NAME = "ejb:/myejb//EjbBean!org.jboss.as.arquillian.container.managed.EjbBusiness";

    /**
     * AS7-3111
     */
    @Test
    public void shouldInjectJndiContext() throws NamingException {
        Assertions.assertNotNull(jndiContext, "AS7-3111: JNDI Context must be injected");
        // Attempt to look up the remote EJB
        final EjbBusiness ejb = (EjbBusiness) jndiContext.lookup(JNDI_NAME);
        Assertions.assertNotNull(ejb, "Could not look up datasource using supplied JNDI Context");
    }
}

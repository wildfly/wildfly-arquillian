/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container;

import org.jboss.arquillian.container.spi.client.container.DeploymentExceptionTransformer;

/**
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class ExceptionTransformer implements DeploymentExceptionTransformer {

    @Override
    public Throwable transform(final Throwable exception) {
        // NOOP Base impl
        return exception;
    }
}

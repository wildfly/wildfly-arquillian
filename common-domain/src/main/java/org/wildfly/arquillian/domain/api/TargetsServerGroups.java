/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.domain.api;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation used for deployments to determine the server groups for the deployment to be deployed to.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Documented
@Retention(RUNTIME)
@Target({ ElementType.METHOD })
public @interface TargetsServerGroups {

    /**
     * The server groups to deploy to.
     *
     * @return the server groups
     */
    TargetsServerGroup[] value();
}

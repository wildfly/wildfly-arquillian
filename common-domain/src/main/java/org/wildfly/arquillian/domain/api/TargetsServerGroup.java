/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.domain.api;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation used for deployments to determine the server group for the deployment to be deployed to.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Documented
@Repeatable(TargetsServerGroups.class)
@Retention(RUNTIME)
@Target({ ElementType.METHOD })
public @interface TargetsServerGroup {

    /**
     * The name of the server group to deploy to.
     *
     * @return the name of the server group
     */
    String value();
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.junit.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.arquillian.junit.condition.RequiresModuleExecutionCondition;

/**
 * Enables or disables a test based on whether the module exists. You can optionally check the version of the module
 * to determine if the modules version is greater than the {@linkplain #minVersion() minimum version}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Inherited
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RequiresModuleExecutionCondition.class)
public @interface RequiresModule {

    /**
     * The minimum version of the module resource.
     * <p>
     * Note that if more than one resource is defined, only the first resource is used to determine the version.
     * </p>
     *
     * @return the minimum version
     */
    String minVersion() default "";

    /**
     * A reference for the issue tracker to be reported in the response for a disabled test.
     *
     * @return the issue reference
     */
    String issueRef() default "";

    /**
     * The reason message for disabled test.
     *
     * @return the reason message
     */
    String reason() default "";

    /**
     * The module that is required for the test to run.
     *
     * @return the module name
     */
    String value();
}

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
 * Enables or disables tests based on the {@link RequiresModule} annotations in the {@linkplain #value() value array}.
 * Only one of the conditions needs to be {@code true} for this to enable tests.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @see RequiresModule
 */
@Inherited
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RequiresModuleExecutionCondition.class)
public @interface AnyOf {

    /**
     * An array of one or more {@link RequiresModule} annotations.
     *
     * @return an array of the annotations
     */
    RequiresModule[] value();
}

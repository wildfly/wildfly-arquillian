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

/**
 * A container for the {@link RequiresModule} annotations to make it a {@link java.lang.annotation.Repeatable}
 * annotation.
 * <p>
 * Note that all modules must exist within the parameters defined for this to return a
 * {@linkplain org.junit.jupiter.api.extension.ConditionEvaluationResult#enabled(String) enabled condition}.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Inherited
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresModules {

    /**
     * An array of one or more {@link RequiresModule} annotations.
     *
     * @return an array of the annotations
     */
    RequiresModule[] value();
}

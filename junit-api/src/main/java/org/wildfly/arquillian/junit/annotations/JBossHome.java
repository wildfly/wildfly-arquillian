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
import org.wildfly.arquillian.junit.condition.JBossHomeParameterResolver;

/**
 * Sets a parameter value to the path of the JBoss Home directory. The parameter can be a {@link java.nio.file.Path},
 * {@link java.io.File} or {@link String}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Inherited
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(JBossHomeParameterResolver.class)
public @interface JBossHome {
}

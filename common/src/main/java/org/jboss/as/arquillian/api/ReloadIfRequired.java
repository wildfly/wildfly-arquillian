/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import org.jboss.as.arquillian.container.ManagementClient;

/**
 * An annotation for a {@link ServerSetupTask} which indicates the server should be reloaded, if required, when the
 * task completes its {@link ServerSetupTask#setup(ManagementClient, String)} or ends in an error. The same will happen
 * for the {@link ServerSetupTask#tearDown(ManagementClient, String)}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface ReloadIfRequired {

    /**
     * The time to wait for the reload to happen. The default is {@code 10}.
     *
     * @return the default time to wait for a reload
     */
    long value() default 10L;

    /**
     * The time unit used for the timeout to wait for a reload. The default is {@link TimeUnit#SECONDS SECONDS}.
     *
     * @return the timeout time unit
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.junit.requires.module;

import org.junit.jupiter.api.Test;
import org.wildfly.arquillian.junit.annotations.RequiresModule;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("NewClassNamingConvention")
@RequiresModule("org.wildfly.arquillian.junit.test.snapshot")
public class RequireSnapshot {

    @Test
    @RequiresModule(value = "org.wildfly.arquillian.junit.test.snapshot", minVersion = "1.0.0.Beta2")
    public void passingVersion() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.arquillian.junit.test.snapshot", minVersion = "1.0.0.Beta3")
    public void skippedVersion() {
    }
}

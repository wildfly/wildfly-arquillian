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
@RequiresModule("org.wildfly.arquillian.junit.test.resource-root")
public class RequireResourceRoot {

    @Test
    public void passing() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.arquillian.junit.test.resource-root", minVersion = "1.0.0")
    public void passingVersion() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.arquillian.junit.test.resource-root", minVersion = "2.0.1")
    public void skippedVersion() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.arquillian.junit.test.resource-root.invalid")
    public void skippedMissingModule() {
    }
}

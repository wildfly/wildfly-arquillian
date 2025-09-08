/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.junit.requires.module;

import org.junit.jupiter.api.Test;
import org.wildfly.arquillian.junit.annotations.AnyOf;
import org.wildfly.arquillian.junit.annotations.RequiresModule;
import org.wildfly.arquillian.junit.annotations.RequiresModules;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("NewClassNamingConvention")
@RequiresModule("org.wildfly.arquillian.junit.test.resource-root")
@RequiresModule("org.wildfly.arquillian.junit.test.client-api")
public class RequireMultiModules {

    @Test
    public void passing() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.arquillian.junit.test.resource-root", minVersion = "1.0.0")
    @RequiresModule(value = "org.wildfly.arquillian.junit.test.client-api", minVersion = "1.0.0.Final")
    public void passingVersion() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.arquillian.junit.test.resource-root", minVersion = "2.0.1")
    @RequiresModule(value = "org.wildfly.arquillian.junit.test.client-api", minVersion = "1.0.0.Final")
    public void skippedVersion() {
    }

    @Test
    @RequiresModules({
            @RequiresModule(value = "org.wildfly.arquillian.junit.test.resource-root", minVersion = "1.0.0"),
            @RequiresModule(value = "org.wildfly.arquillian.junit.test.client-api", minVersion = "1.0.0.Final")
    })
    public void passingVersionRequiresModules() {
    }

    @Test
    @RequiresModules({
            @RequiresModule(value = "org.wildfly.arquillian.junit.test.resource-root", minVersion = "2.0.1"),
            @RequiresModule(value = "org.wildfly.arquillian.junit.test.client-api", minVersion = "1.0.0.Final")
    })
    public void skippedVersionRequiresModules() {
    }

    @Test
    @AnyOf({
            @RequiresModule(value = "org.wildfly.arquillian.junit.test.invalid"),
            @RequiresModule(value = "org.wildfly.arquillian.junit.test.client-api", minVersion = "1.0.0.Final")
    })
    public void passingAnyOf() {
    }

    @Test
    @AnyOf({
            @RequiresModule(value = "org.wildfly.arquillian.junit.test.invalid"),
            @RequiresModule(value = "org.wildfly.arquillian.junit.test.client-api", minVersion = "20.0.0.Final")
    })
    public void skippedAnyOf() {
    }
}

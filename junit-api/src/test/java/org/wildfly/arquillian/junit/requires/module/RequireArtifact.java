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
@RequiresModule("org.wildfly.arquillian.junit.test.artifact")
public class RequireArtifact {

    @Test
    public void passing() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.arquillian.junit.test.artifact", minVersion = "2.0.0")
    public void skippedVersion() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.arquillian.junit.test.artifact.invalid")
    public void skippedMissingModule() {
    }
}

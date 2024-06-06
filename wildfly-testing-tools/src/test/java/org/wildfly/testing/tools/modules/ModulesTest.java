/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.tools.modules;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ModulesTest {

    @BeforeAll
    public static void createModuleDir() throws IOException {
        ModuleEnvironment.createBaseModuleDir();
    }

    @Test
    public void checkModulePath() {
        Assertions.assertEquals(ModuleEnvironment.BASE_MODULE_DIR, Modules.discoverModulePath(),
                "Failed to discover the expected module path. The module path should be the first path which is not said to be immutable.");
    }
}

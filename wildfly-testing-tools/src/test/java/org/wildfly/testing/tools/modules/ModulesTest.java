/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.testing.tools.modules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ModulesTest {

    // The project.build.directory is set as the java.io.tmpdir
    private static final Path EXPECTED_MODULE_DIR = Path.of(System.getProperty("java.io.tmpdir"), "modules").toAbsolutePath();

    @BeforeAll
    public static void createModuleDir() throws IOException {
        if (Files.notExists(EXPECTED_MODULE_DIR)) {
            Files.createDirectories(EXPECTED_MODULE_DIR);
        }
    }

    @Test
    public void checkModulePath() {
        Assertions.assertEquals(EXPECTED_MODULE_DIR, Modules.discoverModulePath(),
                "Failed to discover the expected module path. The module path should be the first path which is not said to be immutable.");
    }
}

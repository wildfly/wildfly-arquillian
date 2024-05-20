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

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class ModuleEnvironment {

    // The project.build.directory is set as the java.io.tmpdir
    static final Path BASE_MODULE_DIR = Path.of(System.getProperty("java.io.tmpdir"), "modules").toAbsolutePath();

    static void createBaseModuleDir() throws IOException {
        if (Files.notExists(BASE_MODULE_DIR)) {
            Files.createDirectories(BASE_MODULE_DIR);
        }
    }
}

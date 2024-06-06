/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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

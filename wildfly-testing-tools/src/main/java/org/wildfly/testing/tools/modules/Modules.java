/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.testing.tools.modules;

import java.io.File;
import java.nio.file.Path;

/**
 * Utilities for JBoss Modules.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @deprecated use the new WildFly Testing Tools project
 */
@Deprecated(forRemoval = true, since = "6.0")
public class Modules {

    /**
     * Discovers the JBoss Module directory to use. Use the {@code org.wildfly.testing.tools.modules.immutable.paths}
     * system property to add a list of paths, separated by a {@linkplain File#pathSeparatorChar path separator}, which
     * are immutable. The first non-immutable path is returned.
     *
     * <p>
     * Paths may be immutable for testing due to not wanting to pollute a local installation of the server.
     * </p>
     *
     * @return the JBoss Modules path where modules can be stored
     *
     * @throws IllegalStateException if there is no module path found or the only module paths are immutable paths
     */
    public static Path discoverModulePath() {
        return org.wildfly.testing.tools.module.Modules.discoverModulePath();
    }
}

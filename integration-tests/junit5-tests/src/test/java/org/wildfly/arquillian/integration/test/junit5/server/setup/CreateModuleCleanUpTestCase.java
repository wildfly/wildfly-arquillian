/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.junit5.server.setup;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * This test must be executed in a different process as {@link CreateModuleServerSetupTaskTestCase} and run after the
 * test. The reason being {@link CreateModuleServerSetupTaskTestCase} creates a module. In the case of Windows, that
 * module cannot be deleted until the test JVM exits.
 * <p>
 * This is not an ideal test scenario, but we do need to verify the module has been deleted.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Tag("cleanup-test")
public class CreateModuleCleanUpTestCase {

    @AfterAll
    public static void cleanUp() throws IOException {
        Files.deleteIfExists(CreateModuleServerSetupTaskTestCase.MARKER_FILE);
    }

    /**
     * Tests that the module created in the {@link CreateModuleServerSetupTaskTestCase} was deleted. Note that this
     * test will fail if the marker file from the previous test was not created.
     */
    @Test
    public void assertModuleCleanUp() {
        Assertions.assertTrue(Files.exists(CreateModuleServerSetupTaskTestCase.MARKER_FILE), () -> String
                .format("Test %s was not executed before this test.", CreateModuleServerSetupTaskTestCase.class.getName()));
        CreateModuleServerSetupTaskTestCase.assertModuleDeleted();
    }
}

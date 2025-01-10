/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed.agent;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
public class AddAgentTestCase {

    @Deployment
    public static WebArchive create() {
        return ShrinkWrap.create(WebArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void agentSet() throws Exception {
        final Path logFile = Paths.get(System.getProperty("jboss.log.dir"), "server.log");
        Assertions.assertTrue(Files.exists(logFile), String.format("Log file %s does not exist.", logFile));
        try (BufferedReader reader = Files.newBufferedReader(logFile, StandardCharsets.UTF_8)) {
            boolean found = false;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(LoggingAgent.MSG)) {
                    found = true;
                    break;
                }
            }
            Assertions.assertTrue(found,
                    String.format("Expected to filed line container \"%s\" in %s.", LoggingAgent.MSG, logFile));
        }
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ApplicationScoped
public class Protocol {

    public String getProtocol() {
        try (InputStream in = Protocol.class.getResourceAsStream("/protocol.txt")) {
            if (in != null) {
                // Read the input stream into a string
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    return reader.readLine();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

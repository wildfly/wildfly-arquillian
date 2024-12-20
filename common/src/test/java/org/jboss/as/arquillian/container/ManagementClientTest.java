/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ManagementClientTest {

    @Test
    void shouldParseBindAllAsLocalhost() {
        String sourceIp = "0.0.0.0";
        String parsedIp = ManagementClient.formatIP(sourceIp);
        assertEquals("127.0.0.1", parsedIp);
    }

    @Test
    void shouldParseLocalIPAsNormalIP() {
        String sourceIp = "10.1.2.3";
        String formattedIp = ManagementClient.formatIP(sourceIp);
        assertEquals(sourceIp, formattedIp);
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container;

import org.junit.Assert;
import org.junit.Test;

public class ManagementClientTest {

    @Test
    public void shouldParseBindAllAsLocalhost() {
        String sourceIp = "0.0.0.0";
        String parsedIp = ManagementClient.formatIP(sourceIp);
        Assert.assertEquals("127.0.0.1", parsedIp);
    }

    @Test
    public void shouldParseLocalIPAsNormalIP() {
        String sourceIp = "10.1.2.3";
        String formattedIp = ManagementClient.formatIP(sourceIp);
        Assert.assertEquals(sourceIp, formattedIp);
    }
}

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

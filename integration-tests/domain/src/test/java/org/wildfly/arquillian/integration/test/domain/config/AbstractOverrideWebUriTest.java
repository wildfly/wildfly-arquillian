/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.domain.config;

import java.net.URI;
import java.net.URL;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
@Tag("OverrideWebUri")
abstract class AbstractOverrideWebUriTest {

    @ArquillianResource
    @TargetsContainer("master:server-one")
    protected URL url;

    @ArquillianResource
    @TargetsContainer("master:server-one")
    private URI uri;

    @Test
    public void url() {
        Assertions.assertNotNull(url, "URL was null and not injected");
        testUrl(url);
    }

    @Test
    @RunAsClient
    public void urlClient() {
        Assertions.assertNotNull(url, "URL was null and not injected");
        testUrl(url);
    }

    @Test
    public void uri() {
        Assertions.assertNotNull(uri, "URI was null and not injected");
        testUri(uri);
    }

    @Test
    @RunAsClient
    public void uriClient() {
        Assertions.assertNotNull(uri, "URI was null and not injected");
        testUri(uri);
    }

    private static void testUrl(final URL url) {
        Assertions.assertEquals("https", url.getProtocol());
        Assertions.assertEquals("127.0.0.1", url.getHost());
        Assertions.assertEquals(8443, url.getPort());
    }

    private static void testUri(final URI uri) {
        Assertions.assertEquals("https", uri.getScheme());
        Assertions.assertEquals("127.0.0.1", uri.getHost());
        Assertions.assertEquals(8443, uri.getPort());
    }
}

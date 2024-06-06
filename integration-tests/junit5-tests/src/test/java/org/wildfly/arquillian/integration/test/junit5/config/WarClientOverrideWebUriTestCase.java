/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.junit5.config;

import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.arquillian.integration.test.junit5.GreeterServlet;
import org.wildfly.security.ssl.SSLContextBuilder;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class WarClientOverrideWebUriTestCase extends AbstractOverrideWebUriTest {

    private static final X509TrustManager TRUST_ALL = new X509ExtendedTrustManager() {
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket)
                throws CertificateException {
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine)
                throws CertificateException {
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    @Deployment(testable = false)
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, WarClientOverrideWebUriTestCase.class.getSimpleName() + ".war")
                .addClass(GreeterServlet.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @RunAsClient
    public void httpsConnection() throws Exception {
        final SSLContext sslContext = new SSLContextBuilder()
                .setTrustManager(TRUST_ALL)
                .setClientMode(true)
                .setNeedClientAuth(false)
                .build()
                .create();
        final HttpClient client = HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        final HttpResponse<String> response = client.send(
                HttpRequest.newBuilder(URI.create(url.toString() + GreeterServlet.URL_PATTERN))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(200, response.statusCode());
        final String body = response.body();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(GreeterServlet.GREETING, body);
    }
}

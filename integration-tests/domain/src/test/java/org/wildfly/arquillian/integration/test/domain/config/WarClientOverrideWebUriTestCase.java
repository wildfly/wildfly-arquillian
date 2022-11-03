/*
 * Copyright 2022 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.arquillian.integration.test.domain.config;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.arquillian.domain.api.TargetsServerGroup;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class WarClientOverrideWebUriTestCase extends AbstractOverrideWebUriTest {

    private static final X509TrustManager TRUST_ALL = new X509TrustManager() {
        @Override
        public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    @Deployment(testable = false)
    @TargetsServerGroup("main-server-group")
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, WarClientOverrideWebUriTestCase.class.getSimpleName() + ".war")
                .addClass(GreeterServlet.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @RunAsClient
    public void httpsConnection() throws Exception {
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[] {TRUST_ALL}, new SecureRandom());
        final OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .hostnameVerifier((hostname, session) -> true)
                .sslSocketFactory(sslContext.getSocketFactory(), TRUST_ALL)
                .build();
        final Request request = new Request.Builder()
                .url(url + GreeterServlet.URL_PATTERN)
                .build();
        final Call call = client.newCall(request);
        try (final Response response = call.execute()) {
            Assertions.assertEquals(200, response.code());
            final ResponseBody body = response.body();
            Assertions.assertNotNull(body);
            Assertions.assertEquals(GreeterServlet.GREETING, body.string());
        }
    }
}

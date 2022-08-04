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

package org.wildfly.arquillian.integration.test.protocol;

import java.net.URI;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class ClientTestCase {
    @ArquillianResource
    private URI uri;

    @Deployment
    public static WebArchive create() {
        return ShrinkWrap.create(WebArchive.class, ClientTestCase.class.getSimpleName() + ".war")
                .addClasses(ProtocolResource.class, RestActivator.class, Protocol.class);
    }

    @Test
    public void testProtocol() {
        final Client restClient = ClientBuilder.newClient();
        try (
                Response response = restClient.target(UriBuilder.fromUri(uri)
                                .path("/rest/protocol"))
                        .request().get()
        ) {
            Assertions.assertEquals(Response.Status.OK, response.getStatusInfo());
            final String body = response.readEntity(String.class);
            Assertions.assertNotNull(body);
            Assertions.assertEquals(System.getProperty("arq.protocol", ""), body);
        } finally {
            restClient.close();
        }
    }
}

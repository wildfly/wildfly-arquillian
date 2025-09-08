/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.junit.integration;

import java.net.URI;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.container.annotation.ArquillianTest;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wildfly.arquillian.junit.annotations.RequiresModule;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ArquillianTest
@RunAsClient
public class ClientRequiresModuleIT {

    @ArquillianResource
    private URI uri;

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, ClientRequiresModuleIT.class.getSimpleName() + ".war")
                .addClass(Greeter.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @RequiresModule("org.jboss.as.ejb3")
    @Test
    public void expectSkipped() {
        Assertions.fail("Test should have been skipped.");
    }

    @RequiresModule("org.jboss.as.ejb3")
    @RequiresModule(value = "jakarta.ws.rs.api", minVersion = "3.1.0")
    @Test
    public void expectSkippedTwoRequiredModules() {
        Assertions.fail("Test should have been skipped.");
    }

    @RequiresModule(value = "jakarta.ws.rs.api", minVersion = "3.1.0")
    @Test
    public void pass() {
        makeRequest();
    }

    @RequiresModule(value = "jakarta.ws.rs.api", minVersion = "3.1.0")
    @RequiresModule(value = "org.jboss.as.jaxrs", minVersion = "27.0.0.Final")
    @Test
    public void passTwoRequiredModules() {
        makeRequest();
    }

    private void makeRequest() {
        try (Client client = ClientBuilder.newClient()) {
            try (Response response = client.target(UriBuilder.fromUri(uri).path("/greeter")).request().get()) {
                Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(), () -> String
                        .format("Response status code %d: %s", response.getStatus(), response.readEntity(String.class)));
                Assertions.assertEquals("Hello!", response.readEntity(String.class));
            }
        }
    }

    @ApplicationScoped
    public static class Greeter {

        public String hello() {
            return "Hello!";
        }
    }

    @ApplicationPath("/")
    public static class RestActivator extends Application {

    }

    @Path("/greeter")
    public static class GreeterResource {
        @Inject
        private Greeter greeter;

        @GET
        public String hello() {
            return greeter.hello();
        }
    }
}

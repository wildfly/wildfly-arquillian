/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.protocol;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Path("/protocol")
@ApplicationScoped
public class ProtocolResource {

    @Inject
    private Protocol protocol;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String greet() {
        return protocol.getProtocol();
    }
}

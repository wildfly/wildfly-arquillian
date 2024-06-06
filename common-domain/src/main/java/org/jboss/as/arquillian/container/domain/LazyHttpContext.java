/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.domain;

import java.util.List;

import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.as.arquillian.container.domain.Domain.Server;

/**
 * We lookup deployment context lazy because the server in the server-group might not be started during deploy time.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class LazyHttpContext extends HTTPContext {

    private Server server;
    private String deploymentName;
    private ManagementClient client;

    private HTTPContext context = null;

    public LazyHttpContext(Server server, String deploymentName, ManagementClient client) {
        super("localhost", -1);

        this.server = server;
        this.deploymentName = deploymentName;
        this.client = client;
    }

    @Override
    public String getName() {
        return server.getContainerName();
    }

    @Override
    public String getHost() {
        initiateContext();
        return context.getHost();
    }

    @Override
    public int getPort() {
        initiateContext();
        return context.getPort();
    }

    @Override
    public HTTPContext add(Servlet servlet) {
        initiateContext();
        return context.add(servlet);
    }

    @Override
    public List<Servlet> getServlets() {
        initiateContext();
        return context.getServlets();
    }

    @Override
    public Servlet getServletByName(String name) {
        initiateContext();
        return context.getServletByName(name);
    }

    private void initiateContext() {
        if (context == null) {
            context = client.getHTTPDeploymentMetaData(server, deploymentName);
        }
    }
}

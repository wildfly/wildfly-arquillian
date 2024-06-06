/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container.managed;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A basic "hello world" servlet for testing client-side invocation in an Arquillian test.
 *
 * @author <a href="http://community.jboss.org/people/dan.j.allen">Dan Allen</a>
 */
@WebServlet(name = "HelloWorldServlet", urlPatterns = HelloWorldServlet.URL_PATTERN)
public class HelloWorldServlet extends HttpServlet {
    private static final long serialVersionUID = 2202174128107400113L;

    public static final String URL_PATTERN = "/greet";

    public static final String GREETING = "Hello, World";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        res.getWriter().append(GREETING);
    }
}

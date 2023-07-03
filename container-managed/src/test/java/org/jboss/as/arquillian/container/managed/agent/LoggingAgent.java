/*
 * Copyright 2023 Red Hat, Inc.
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

package org.jboss.as.arquillian.container.managed.agent;

import java.lang.instrument.Instrumentation;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class LoggingAgent {
    public static final String LOGGER_NAME = LoggingAgent.class.getName();
    public static final String MSG = "This is a message from the agent";

    public static void premain(final String args, final Instrumentation instrumentation) throws ClassNotFoundException {
        assert "org.jboss.logmanager.LogManager".equals(System.getProperty(
                "java.util.logging.manager")) : "Expected the java.util.logging.manager to be set to org.jboss.logmanager.LogManager but was "
                        + System.getProperty("java.util.logging.manager") + System.lineSeparator();
        final LogManager logManager = LogManager.getLogManager();
        assert "org.jboss.logmanager.LogManager".equals(
                logManager.getClass().getName()) : "Expected the LogManager to be org.jboss.logmanager.LogManager but was "
                        + logManager.getClass().getName() + System.lineSeparator();
        final Logger logger = logManager.getLogger(LOGGER_NAME);
        logger.info(MSG);
    }
}

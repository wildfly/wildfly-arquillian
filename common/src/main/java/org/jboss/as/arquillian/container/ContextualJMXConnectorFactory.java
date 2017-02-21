/*
 * Copyright 2017 Red Hat, Inc.
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

package org.jboss.as.arquillian.container;

import java.io.IOException;
import java.util.Map;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;

import org.wildfly.common.context.Contextual;
import org.wildfly.common.function.ExceptionFunction;

/**
 * A {@link JMXConnector} which wraps invocations of the delegate client in the provided
 * {@linkplain Contextual context}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class ContextualJMXConnectorFactory {

    private ContextualJMXConnectorFactory() {
    }

    /**
     * Creates and connects a new connector.
     * <p>
     * The {@link JMXConnectorFactory#connect(JMXServiceURL)} is invoked with the {@code context} parameter. The
     * parameter is then used to create a new {@link ContextualJMXConnectorFactory}.
     * </p>
     *
     * @param context the context to use
     * @param url     the JMX URL
     * @param env     the environment
     *
     * @return a new JMX connector
     *
     * @throws IOException if the connector client or the connection cannot be made because of a communication problem
     */
    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static JMXConnector connect(final Contextual<?> context, final JMXServiceURL url, final Map<String, Object> env) throws IOException {
        if (context == null) {
            return JMXConnectorFactory.connect(url, env);
        }
        return context.runExFunction((ExceptionFunction<Object, JMXConnector, IOException>) o ->
                new ContextualJMXConnector(context, JMXConnectorFactory.connect(url, env)), null);
    }

    private static class ContextualJMXConnector implements JMXConnector {
        private final Contextual<?> context;
        private final JMXConnector delegate;

        private ContextualJMXConnector(final Contextual<?> context, final JMXConnector delegate) {
            this.context = context;
            this.delegate = delegate;
        }

        @Override
        public void connect() throws IOException {
            context.runExConsumer(o -> delegate.connect(), null);
        }

        @Override
        public void connect(final Map<String, ?> env) throws IOException {
            context.runExConsumer(o -> delegate.connect(env), null);
        }

        @Override
        public MBeanServerConnection getMBeanServerConnection() throws IOException {
            return context.runExFunction(o -> delegate.getMBeanServerConnection(), null);
        }

        @Override
        public MBeanServerConnection getMBeanServerConnection(final Subject delegationSubject) throws IOException {
            return context.runExFunction(o -> delegate.getMBeanServerConnection(delegationSubject), null);
        }

        @Override
        public void close() throws IOException {
            context.runExConsumer(o -> delegate.close(), null);
        }

        @Override
        public void addConnectionNotificationListener(final NotificationListener listener, final NotificationFilter filter, final Object handback) {
            context.runExConsumer(o -> delegate.addConnectionNotificationListener(listener, filter, handback), null);
        }

        @Override
        public void removeConnectionNotificationListener(final NotificationListener listener) throws ListenerNotFoundException {
            context.runExConsumer(o -> delegate.removeConnectionNotificationListener(listener), null);
        }

        @Override
        public void removeConnectionNotificationListener(final NotificationListener l, final NotificationFilter f, final Object handback) throws ListenerNotFoundException {
            context.runExConsumer(o -> delegate.removeConnectionNotificationListener(l, f, handback), null);
        }

        @Override
        public String getConnectionId() throws IOException {
            return context.runExFunction(o -> delegate.getConnectionId(), null);
        }
    }
}

/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.managed;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.as.arquillian.container.ManagementClient;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Ensures a {@link ManagementClient} can be injected to a client test.
 *
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class ClientInjectManagementClientTestCase extends InjectManagementClientTestCase {
}

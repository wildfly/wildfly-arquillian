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

package org.jboss.as.arquillian.container.managed;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Ensures the order of the tests to execute in the correct order.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        ServerSetupDeploymentTestCase.class,
        ServerSetupAssumptionViolationTestCase.class,
        ServerSetupUnmanagedAssumptionViolationTestCase.class,
        ServerSetupAfterClassTestCase.class
})
public class ServerSetupTestSuite {
    static final String SYSTEM_PROPERTY_KEY = "server.setup.key";
    static final String SYSTEM_PROPERTY_VALUE = "server.setup.value";
}

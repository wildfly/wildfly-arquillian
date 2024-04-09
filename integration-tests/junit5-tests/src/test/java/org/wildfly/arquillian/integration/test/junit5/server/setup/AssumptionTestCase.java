/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.arquillian.integration.test.junit5.server.setup;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Tag("engine-test-kit")
public class AssumptionTestCase {

    @ParameterizedTest
    @ValueSource(classes = {
            AssumptionServerSetup.class,
            InContainerAssumptionServerSetup.class,
            UnmanagedAssumptionServerSetup.class
    })
    public void serverSetup(final Class<?> testClass) {
        final var events = EngineTestKit.engine("junit-jupiter")
                .selectors(DiscoverySelectors.selectMethod(testClass, "failIfExecuted"))
                // .enableImplicitConfigurationParameters(true)
                .execute()
                .allEvents();

        // Should have a single aborted event and no failures.
        events.assertStatistics((stats) -> stats.aborted(1L));
        events.assertStatistics((stats) -> stats.failed(0L));
    }
}

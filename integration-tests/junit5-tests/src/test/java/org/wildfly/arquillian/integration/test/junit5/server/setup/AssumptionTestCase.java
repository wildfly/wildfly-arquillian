/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
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

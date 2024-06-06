/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.setup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.client.helpers.Operations.CompositeOperationBuilder;
import org.jboss.dmr.ModelNode;

/**
 * A setup task for configuring loggers for tests.
 * <p>
 * You can define the log levels and logger names in two ways. The first is to pass a map of known logger levels with
 * associated logger names to the {@linkplain ConfigureLoggingSetupTask#ConfigureLoggingSetupTask(Map) constructor}.
 * The other is via a system property.
 * </p>
 * <p>
 * To set the levels and logger names via a system property, use a key of {@code wildfly.logging.level.${level}} where
 * {@code level} is one of the following:
 * <ol>
 * <li>all</li>
 * <li>trace</li>
 * <li>debug</li>
 * <li>info</li>
 * <li>warn</li>
 * <li>error</li>
 * <li>off</li>
 * </ol>
 *
 * The value for each property is a comma delimited set of logger names.
 * <p>
 * Example:
 *
 * <pre>
 *       {@code -Dwildfly.logging.level.debug=org.wildfly.security,org.jboss.resteasy}
 *   </pre>
 * </p>
 * <p>
 * When using the constructor, the map should consist of a known log level as the key and loggers to be associated with
 * that level as the value of the map. Example:
 *
 * <pre>
 *     {@code
 *          public class WildFlyLoggingSetupTask extends ConfigurationLoggingSetupTask {
 *              public WildFlyLoggingSetupTask() {
 *                  super(Map.of("DEBUG", Set.of("org.wildfly.core", "org.wildfly"}));
 *              }
 *          }
 *     )
 * </pre>
 *
 * Note that when using the map constructor, you can still use the system property and the maps will be merged.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ConfigureLoggingSetupTask implements ServerSetupTask {
    private final String handlerType;
    private final String handlerName;
    private final Map<String, Set<String>> logLevels;
    private final BlockingDeque<ModelNode> tearDownOps;

    /**
     * Creates a new setup task which configures the {@code console-handler=CONSOLE} handler to allow all log levels.
     * Then configures, either by modifying or adding, the loggers represented by the values from the system properties.
     */
    public ConfigureLoggingSetupTask() {
        this(Map.of());
    }

    /**
     * Creates a new setup task which configures the handler to allow all log levels. Then configures, either by
     * modifying or adding, the loggers represented by system properties.
     *
     * @param handlerType the handler type which should be modified to ensure it allows all log levels, if {@code null}
     *                        {@code console-handler} will be used
     * @param handlerName the name of the handler which should be modified to ensure it allows all log levels, if {@code null}
     *                        {@code console-handler} will be used
     */
    public ConfigureLoggingSetupTask(final String handlerType, final String handlerName) {
        this(handlerType, handlerName, Map.of());
    }

    /**
     * Creates a new setup task which configures the {@code console-handler=CONSOLE} handler to allow all log levels.
     * Then configures, either by modifying or adding, the loggers represented by the values of the map passed in. The
     * key of the map is the level desired for each logger.
     * <p>
     * The map consists of levels as the key and a set of logger names as the value for each level.
     * </p>
     *
     * @param logLevels the map of levels and loggers
     */
    public ConfigureLoggingSetupTask(final Map<String, Set<String>> logLevels) {
        this(null, null, logLevels);
    }

    /**
     * Creates a new setup task which configures the handler to allow all log levels. Then configures, either by
     * modifying or adding, the loggers represented by the values of the map passed in. The key of the map is the level
     * desired for each logger.
     * <p>
     * If the {@code handlerType} is {@code null} the value will be {@code console-handler}. If the {@code handlerName}
     * is {@code null} the value used will be {@code CONSOLE}.
     * </p>
     * <p>
     * The map consists of levels as the key and a set of logger names as the value for each level.
     * </p>
     *
     * @param handlerType the handler type which should be modified to ensure it allows all log levels, if {@code null}
     *                        {@code console-handler} will be used
     * @param handlerName the name of the handler which should be modified to ensure it allows all log levels, if {@code null}
     *                        {@code console-handler} will be used
     * @param logLevels   the map of levels and loggers
     */
    public ConfigureLoggingSetupTask(final String handlerType, final String handlerName,
            final Map<String, Set<String>> logLevels) {
        this.handlerType = handlerType == null ? "console-handler" : handlerType;
        this.handlerName = handlerName == null ? "CONSOLE" : handlerName;
        this.logLevels = createMap(logLevels);
        this.tearDownOps = new LinkedBlockingDeque<>();
    }

    @Override
    public void setup(final ManagementClient client, final String containerId) throws Exception {
        final CompositeOperationBuilder builder = CompositeOperationBuilder.create();
        ModelNode address = Operations.createAddress("subsystem", "logging", handlerType, handlerName);

        // We need the current level to reset it when done
        ModelNode currentValue = executeOp(client.getControllerClient(),
                Operations.createReadAttributeOperation(address, "level"));
        if (currentValue.isDefined()) {
            tearDownOps.add(Operations.createWriteAttributeOperation(address, "level", currentValue.asString()));
        }

        builder.addStep(Operations.createUndefineAttributeOperation(address, "level"));
        for (Map.Entry<String, Set<String>> entry : logLevels.entrySet()) {
            for (String logger : entry.getValue()) {
                if (logger.isBlank()) {
                    address = Operations.createAddress("subsystem", "logging", "root-logger", "ROOT");
                } else {
                    address = Operations.createAddress("subsystem", "logging", "logger", logger);
                }
                builder.addStep(createLoggerOp(client.getControllerClient(), address, entry.getKey()));
            }
        }
        executeOp(client.getControllerClient(), builder.build());
    }

    @Override
    public void tearDown(final ManagementClient managementClient, final String containerId) throws Exception {
        // Create a composite operation with all the tear-down operations
        final CompositeOperationBuilder builder = CompositeOperationBuilder.create();
        ModelNode removeOp;
        while ((removeOp = tearDownOps.pollFirst()) != null) {
            builder.addStep(removeOp);
        }
        executeOp(managementClient.getControllerClient(), builder.build());
    }

    private ModelNode createLoggerOp(final ModelControllerClient client, final ModelNode address, final String level)
            throws IOException {
        // First check if the logger exists
        final ModelNode op = Operations.createReadResourceOperation(address);
        final ModelNode result = client.execute(op);
        if (Operations.isSuccessfulOutcome(result)) {
            // Get the current level from te result
            final ModelNode loggerConfig = Operations.readResult(result);
            if (loggerConfig.hasDefined("level")) {
                tearDownOps.add(Operations.createWriteAttributeOperation(address, "level", loggerConfig.get("level")
                        .asString()));
            }
            return Operations.createWriteAttributeOperation(address, "level", level);
        }
        tearDownOps.add(Operations.createRemoveOperation(address));
        final ModelNode addOp = Operations.createAddOperation(address);
        addOp.get("level").set(level);
        return addOp;
    }

    private ModelNode executeOp(final ModelControllerClient client, final ModelNode op) throws IOException {
        return executeOp(client, Operation.Factory.create(op));
    }

    private ModelNode executeOp(final ModelControllerClient client, final Operation op) throws IOException {
        final ModelNode result = client.execute(op);
        if (!Operations.isSuccessfulOutcome(result)) {
            throw new RuntimeException(Operations.getFailureDescription(result).asString());
        }
        return Operations.readResult(result);
    }

    private static Map<String, Set<String>> createMap(final Map<String, Set<String>> toMerge) {
        // We only allow a known set of levels
        final Map<String, Set<String>> logLevels = new HashMap<>();
        addLoggingConfig(logLevels, "all");
        addLoggingConfig(logLevels, "trace");
        addLoggingConfig(logLevels, "debug");
        addLoggingConfig(logLevels, "info");
        addLoggingConfig(logLevels, "warn");
        addLoggingConfig(logLevels, "error");
        addLoggingConfig(logLevels, "off");
        return Map.copyOf(merge(logLevels, toMerge));
    }

    private static void addLoggingConfig(final Map<String, Set<String>> map, final String level) {
        final String value = System.getProperty("wildfly.logging.level." + level);
        if (value != null) {
            final Set<String> names = Set.of(value.split(","));
            if (!names.isEmpty()) {
                map.put(level.toUpperCase(Locale.ROOT), names);
            }
        }
    }

    private static Map<String, Set<String>> merge(final Map<String, Set<String>> map1, final Map<String, Set<String>> map2) {
        final Map<String, Set<String>> result = new HashMap<>();
        for (var entry : map1.entrySet()) {
            result.put(entry.getKey().toUpperCase(Locale.ROOT), entry.getValue());
        }
        for (final var entry : map2.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            final String key = entry.getKey().toUpperCase(Locale.ROOT);
            if (result.containsKey(key)) {
                result.put(key,
                        Stream.concat(result.get(key).stream(), entry.getValue().stream())
                                .collect(Collectors.toSet()));
            } else {
                result.put(key, Set.copyOf(entry.getValue()));
            }
        }
        return Map.copyOf(result);
    }
}

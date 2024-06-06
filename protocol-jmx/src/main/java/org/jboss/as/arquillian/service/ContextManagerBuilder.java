/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.service;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.SetupAction;

/**
 * Builds a {@link ContextManager}
 *
 * @author Stuart Douglas
 * @author <a href="mailto:alr@jboss.org">Andrew Lee Rubinger</a>
 */
public class ContextManagerBuilder {

    private final List<SetupAction> setupActions = new ArrayList<SetupAction>();
    private final ArquillianConfig config;

    ContextManagerBuilder(ArquillianConfig config) {
        this.config = config;
    }

    /**
     * Adds a {@link SetupAction} to the builder. This action will be run by the {@link ContextManager} in the order it was
     * added to the builder.
     *
     * @param action The {@link SetupAction} to add to the builder
     * @return this
     */
    public ContextManagerBuilder add(final SetupAction action) {
        setupActions.add(action);
        return this;
    }

    public ContextManagerBuilder addAll(final DeploymentUnit deploymentUnit) {
        List<SetupAction> actions = deploymentUnit.getAttachment(Attachments.SETUP_ACTIONS);
        if (actions != null) {
            setupActions.addAll(actions);
        }
        return this;
    }

    public ContextManager build() {
        return new ContextManager(config, setupActions);
    }

}

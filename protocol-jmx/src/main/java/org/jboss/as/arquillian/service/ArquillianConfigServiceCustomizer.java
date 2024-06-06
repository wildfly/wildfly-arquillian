/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.service;

import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceController;

/**
 * Allows to customize ArquillianConfig service (e.g. get values injected and
 * add dependencies) and to use the dependencies in test class loading.
 *
 * @author <a href="mailto:arcadiy@ivanov.biz">Arcadiy Ivanov</a>
 * @version $Revision: $
 */
public interface ArquillianConfigServiceCustomizer {

    void customizeService(ArquillianConfig arquillianConfig, ServiceBuilder<ArquillianConfig> builder,
            ServiceController<?> depController);

    void customizeLoadClass(DeploymentUnit depUnit, Class<?> testClass);
}

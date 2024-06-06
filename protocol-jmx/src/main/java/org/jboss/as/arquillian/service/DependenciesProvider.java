/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.service;

import java.util.Set;

import org.jboss.modules.ModuleIdentifier;

/**
 * Implemented by a {@link org.jboss.arquillian.container.test.spi.RemoteLoadableExtension} to provide additional dependencies.
 *
 * @author Thomas.Diesler@jboss.com
 * @since 19-May-2013
 */
public interface DependenciesProvider {

    /** The set of extension dependencies */
    Set<ModuleIdentifier> getDependencies();
}
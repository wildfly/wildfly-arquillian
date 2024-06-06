/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container;

/**
 * A standard implementation for the {@link ContainerDescription}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class StandardContainerDescription implements ContainerDescription {
    private final org.wildfly.plugin.tools.ContainerDescription delegate;
    private final ModelVersion modelVersion;

    StandardContainerDescription(final org.wildfly.plugin.tools.ContainerDescription delegate) {
        this.delegate = delegate;
        final var modelVersion = delegate.getModelVersion();
        this.modelVersion = new ModelVersion(modelVersion.major(), modelVersion.minor(), modelVersion.micro());
    }

    @Override
    public String getProductName() {
        return delegate.getProductName();
    }

    @Override
    public String getProductVersion() {
        return delegate.getProductVersion();
    }

    @Override
    public String getReleaseCodename() {
        return "";
    }

    @Override
    public String getReleaseVersion() {
        return delegate.getReleaseVersion();
    }

    @Override
    public ModelVersion getModelVersion() {
        return modelVersion;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}

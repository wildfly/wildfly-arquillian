/*
 * Copyright 2015 Red Hat, Inc.
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

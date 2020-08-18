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

    static final StandardContainerDescription NULL_DESCRIPTION = new StandardContainerDescription("WildFly",
            null, null, null, ModelVersion.DEFAULT);

    private final String productName;
    private final String productVersion;
    private final String releaseCodename;
    private final String releaseVersion;
    private final ModelVersion modelVersion;

    StandardContainerDescription(final String productName, final String productVersion,
                                 final String releaseCodename, final String releaseVersion,
                                 final ModelVersion modelVersion) {
        this.productName = productName;
        this.productVersion = productVersion;
        this.releaseCodename = releaseCodename;
        this.releaseVersion = releaseVersion;
        this.modelVersion = modelVersion;
    }

    @Override
    public String getProductName() {
        return productName;
    }

    @Override
    public String getProductVersion() {
        return productVersion;
    }

    @Override
    public String getReleaseCodename() {
        return releaseCodename;
    }

    @Override
    public String getReleaseVersion() {
        return releaseVersion;
    }

    @Override
    public ModelVersion getModelVersion() {
        return modelVersion;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder(64);
        result.append(productName);
        if (productVersion != null) {
            result.append(' ').append(productVersion);
            if (releaseCodename != null) {
                result.append(' ').append('"').append(releaseCodename).append('"');
            }
            if (releaseVersion != null) {
                result.append(" (WildFly Core ").append(releaseVersion).append(')');
            }
        } else {
            if (releaseVersion != null) {
                result.append(' ').append(releaseVersion);
            }
            if (releaseCodename != null) {
                result.append(' ').append('"').append(releaseCodename).append('"');
            }
        }
        return result.toString();
    }
}

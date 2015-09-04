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

import java.io.IOException;

import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

/**
 * A standard implementation for the {@link ContainerDescription}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class StandardContainerDescription implements ContainerDescription {

    static final StandardContainerDescription NULL_DESCRIPTION = new StandardContainerDescription("WildFly", null, null, null);

    private final String productName;
    private final String productVersion;
    private final String releaseCodename;
    private final String releaseVersion;

    private StandardContainerDescription(final String productName, final String productVersion, final String releaseCodename, final String releaseVersion) {
        this.productName = productName;
        this.productVersion = productVersion;
        this.releaseCodename = releaseCodename;
        this.releaseVersion = releaseVersion;
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

    /**
     * Queries the running container and attempts to lookup the information from the running container.
     *
     * @param client the client used to execute the management operation
     *
     * @return the container description
     *
     * @throws IOException if an error occurs while executing the management operation
     */
    public static StandardContainerDescription lookup(final ManagementClient client) throws IOException {
        final ModelNode op = Operations.createReadResourceOperation(new ModelNode().setEmptyList());
        final ModelNode result = client.getControllerClient().execute(op);
        if (Operations.isSuccessfulOutcome(result)) {
            final ModelNode model = Operations.readResult(result);
            final String productName;
            if (model.hasDefined("product-name")) {
                productName = model.get("product-name").asString();
            } else {
                productName = "WildFly";
            }

            String productVersion = null;
            if (model.hasDefined("product-version")) {
                productVersion = model.get("product-version").asString();
            }

            String releaseCodename = null;
            if (model.hasDefined("release-codename")) {
                releaseCodename = model.get("release-codename").asString();
            }

            String releaseVersion = null;
            if (model.hasDefined("release-version")) {
                releaseVersion = model.get("release-version").asString();
            }
            return new StandardContainerDescription(productName, productVersion, releaseCodename, releaseVersion);
        } else {
            Logger.getLogger(StandardContainerDescription.class).errorf("Failed to read the root resource: ", Operations.getFailureDescription(result));
        }

        return NULL_DESCRIPTION;
    }
}

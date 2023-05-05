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
 * Information about the running container.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface ContainerDescription {

    /**
     * Returns the name of the product.
     *
     * @return the name of the product
     */
    String getProductName();

    /**
     * Returns the product version, if defined, or {@code null} if the product version was not defined.
     *
     * @return the product version or {@code null} if not defined
     */
    String getProductVersion();

    /**
     * Returns the release codename, if defined, or {@code null} if the codename was not defined.
     *
     * @return the codename or {@code null} if not defined
     */
    String getReleaseCodename();

    /**
     * Returns the release version, if defined, or {@code null} if the release version was not defined.
     * <p>
     * Note that in WildFly 9+ this is usually the version for WildFly Core. In WildFly 8 this is the full version.
     * </p>
     *
     * @return the release version or {@code null} if not defined
     */
    String getReleaseVersion();

    /**
     * Returns the root model version.
     *
     * @return the model version
     */
    default ModelVersion getModelVersion() {
        return ModelVersion.DEFAULT;
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
    static ContainerDescription lookup(final ManagementClient client) throws IOException {
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
            final ModelVersion modelVersion = new ModelVersion(
                    model.get("management-major-version").asInt(0),
                    model.get("management-minor-version").asInt(0),
                    model.get("management-micro-version").asInt(0));
            return new StandardContainerDescription(productName, productVersion, releaseCodename, releaseVersion, modelVersion);
        } else {
            Logger.getLogger(ContainerDescription.class).errorf("Failed to read the root resource: ",
                    Operations.getFailureDescription(result));
        }

        return StandardContainerDescription.NULL_DESCRIPTION;
    }

    /**
     * Describes the model version.
     */
    final class ModelVersion {
        static final ModelVersion DEFAULT = new ModelVersion(0, 0, 0);

        private final int major;
        private final int minor;
        private final int micro;

        ModelVersion(final int major, final int minor, final int micro) {
            this.major = major;
            this.minor = minor;
            this.micro = micro;
        }

        /**
         * The major version of the model.
         *
         * @return the major version
         */
        public int getMajor() {
            return major;
        }

        /**
         * The minor version of the model.
         *
         * @return the minor version
         */
        public int getMinor() {
            return minor;
        }

        /**
         * THe micro version of the model.
         *
         * @return the micro version
         */
        public int getMicro() {
            return micro;
        }
    }
}

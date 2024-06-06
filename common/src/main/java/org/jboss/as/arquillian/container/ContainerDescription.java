/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.container;

import java.io.IOException;

/**
 * Information about the running container.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Deprecated(forRemoval = true)
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
        return new StandardContainerDescription(
                org.wildfly.plugin.tools.ContainerDescription.lookup(client.getControllerClient()));
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

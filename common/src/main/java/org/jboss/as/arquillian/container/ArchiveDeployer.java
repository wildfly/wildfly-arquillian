/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.arquillian.container;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentHelper;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * A deployer that uses the {@link ServerDeploymentHelper}.
 *
 * <p>
 * The client is not closed by an instance of this and is the responsibility of the user to clean up the client instance.
 * </p>
 *
 * @author Thomas.Diesler@jboss.com
 * @since 17-Nov-2010
 */
public class ArchiveDeployer {

    private static final Logger log = Logger.getLogger(ArchiveDeployer.class);

    private final ServerDeploymentHelper deployer;
    private final ManagementClient client;

    /**
     * Creates a new deployer for deploying archives.
     * <p>
     * Using this constructor the state of the client connection cannot be validated. This could produce unexpected
     * results if the client is closed and there's an attempt to use an instance of this type.
     * </p>
     *
     * @param modelControllerClient the model controller to use
     *
     * @deprecated use {@link #ArchiveDeployer(ManagementClient)}
     */
    @Deprecated
    public ArchiveDeployer(ModelControllerClient modelControllerClient) {
        this.deployer = new ServerDeploymentHelper(modelControllerClient);
        client = null;
    }

    /**
     * Creates a new deployer for deploying archives.
     *
     * @param client the management client to use
     */
    public ArchiveDeployer(ManagementClient client) {
        this.client = client;
        this.deployer = new ServerDeploymentHelper(client.getControllerClient());
    }

    /**
     * Deploys the archive to a running container.
     *
     * @param archive the archive to deploy
     *
     * @return the runtime name of the deployment
     *
     * @throws DeploymentException   if an error happens during deployment
     * @throws IllegalStateException if the client has been closed
     */
    public String deploy(Archive<?> archive) throws DeploymentException {
        return deployInternal(archive);
    }

    /**
     * Deploys the archive to a running container.
     *
     * @param name  the runtime for the deployment
     * @param input the input stream of a deployment archive
     *
     * @return the runtime name of the deployment
     *
     * @throws DeploymentException   if an error happens during deployment
     * @throws IllegalStateException if the client has been closed
     */
    public String deploy(String name, InputStream input) throws DeploymentException {
        return deployInternal(name, input);
    }

    /**
     * Removes an archive from the running container.
     * <p>
     * All exceptions are caught and logged as a warning. {@link Error Errors} will still be thrown however.
     * </p>
     *
     * @param runtimeName the runtime name for the deployment
     */
    public void undeploy(String runtimeName) {
        try {
            deployer.undeploy(runtimeName);
        } catch (Exception ex) {
            log.warnf(ex, "Cannot undeploy: %s", runtimeName);
        }
    }

    private String deployInternal(Archive<?> archive) throws DeploymentException {
        checkState();
        final InputStream input = archive.as(ZipExporter.class).exportAsInputStream();
        try {
            return deployInternal(archive.getName(), input);
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (IOException e) {
                    log.warnf(e, "Failed to close resource %s", input);
                }
        }
    }

    private String deployInternal(String name, InputStream input) throws DeploymentException {
        checkState();
        try {
            return deployer.deploy(name, input);
        } catch (Exception ex) {
            Throwable rootCause = ex.getCause();
            while (null != rootCause && rootCause.getCause() != null) {
                rootCause = rootCause.getCause();
            }
            throw new DeploymentException("Cannot deploy: " + name, rootCause);
        }
    }

    private void checkState() {
        // Checks the state
        if (client != null && client.isClosed()) {
            throw new IllegalStateException("The client connection has been closed.");
        }
    }
}

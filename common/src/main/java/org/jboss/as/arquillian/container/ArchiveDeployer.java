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
import java.io.InputStream;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.wildfly.plugin.core.Deployment;
import org.wildfly.plugin.core.DeploymentManager;
import org.wildfly.plugin.core.DeploymentResult;
import org.wildfly.plugin.core.UndeployDescription;

/**
 * Allows deployment operations to be executed on a running server.
 *
 * <p>
 * The client is not closed by an instance of this and is the responsibility of the user to clean up the client instance.
 * </p>
 *
 * @author Thomas.Diesler@jboss.com
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @since 17-Nov-2010
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ArchiveDeployer {

    private static final Logger log = Logger.getLogger(ArchiveDeployer.class);

    private final DeploymentManager deploymentManager;
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
        this.deploymentManager = DeploymentManager.Factory.create(modelControllerClient);
        client = null;
    }

    /**
     * Creates a new deployer for deploying archives.
     *
     * @param client the management client to use
     */
    public ArchiveDeployer(ManagementClient client) {
        this.client = client;
        this.deploymentManager = DeploymentManager.Factory.create(client.getControllerClient());
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
        undeploy(runtimeName, true);
    }

    /**
     * Removes an archive from the running container.
     * <p>
     * All exceptions are caught and logged as a warning. {@link Error Errors} will still be thrown however.
     * </p>
     *
     * @param runtimeName   the runtime name for the deployment
     * @param failOnMissing {@code true} if the undeploy should fail if the deployment was not found on the server,
     *                      {@code false} if the deployment does not exist and the undeploy should be ignored
     */
    @SuppressWarnings("SameParameterValue")
    public void undeploy(final String runtimeName, final boolean failOnMissing) {
        checkState();
        try {
            final DeploymentResult result = deploymentManager.undeploy(UndeployDescription.of(runtimeName).setFailOnMissing(failOnMissing));
            if (!result.successful()) {
                log.warnf("Failed to undeploy %s: %s", runtimeName, result.getFailureMessage());
            }
        } catch (Exception ex) {
            log.warnf(ex, "Cannot undeploy: %s", runtimeName);
        }
    }

    /**
     * Checks if the deployment content is on the server.
     *
     * @param name the name of the deployment
     *
     * @return {@code true} if the deployment content exists otherwise {@code false}
     *
     * @throws IOException if a failure occurs communicating with the server
     */
    public boolean hasDeployment(final String name) throws IOException {
        checkState();
        return deploymentManager.hasDeployment(name);
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
        final DeploymentResult result;
        try {
            result = deploymentManager.deploy(Deployment.of(input, name));
        } catch (Exception ex) {
            throw createException("Cannot deploy: " + name, ex);
        }
        if (result.successful()) {
            return name;
        }
        throw new DeploymentException(String.format("Cannot deploy %s: %s", name, result.getFailureMessage()));
    }

    private void checkState() {
        // Checks the state
        if (client != null && client.isClosed()) {
            throw new IllegalStateException("The client connection has been closed.");
        }
    }

    /**
     * Creates a deployment exception with the root cause of the exception adding any other causes as a suppressed
     * exception.
     *
     * @param message the message for the exception
     * @param cause   the first cause
     *
     * @return a deployment exception for the error
     */
    private static DeploymentException createException(final String message, final Throwable cause) {
        final Set<Throwable> causes = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable currentCause = cause;
        Throwable rootCause = currentCause;
        while (currentCause != null) {
            currentCause = currentCause.getCause();
            if (currentCause != null) {
                causes.add(currentCause);
                rootCause = currentCause;
            }
        }
        final DeploymentException result = new DeploymentException(message, rootCause);
        causes.forEach(result::addSuppressed);
        return result;
    }
}

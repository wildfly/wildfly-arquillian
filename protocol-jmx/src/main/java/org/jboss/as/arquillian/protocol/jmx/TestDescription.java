/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.arquillian.protocol.jmx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.spi.TestDeployment;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.ArchiveAsset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.vfs.VirtualFile;

/**
 * A simple definition describing a test deployment.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class TestDescription {

    private static final String PATH = "/META-INF/test-description.properties";
    private static final String TARGET_CONTAINER = "org.jboss.as.arquillian.protocol.jmx.target.container";
    private static final String ARQ_DEPLOYMENT_NAME = "org.jboss.as.arquillian.protocol.jmx.arq.deployment.name";
    private static final String DEPLOYMENT_NAME = "org.jboss.as.arquillian.protocol.jmx.deployment.name";
    private static final Filter<ArchivePath> ROOT_FILTER = (p) -> p.getParent() == null || p.getParent().get().equals("/");

    private final Properties properties;

    private TestDescription(final Properties properties) {
        this.properties = properties;
    }

    /**
     * Gets the test description from the deployment.
     *
     * @param deploymentUnit the deployment unit
     *
     * @return the test description from the deployment
     */
    public static TestDescription from(final DeploymentUnit deploymentUnit) {
        // Get the properties
        final ResourceRoot resourceRoot = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT);
        final VirtualFile testDescription = resourceRoot.getRoot().getChild(TestDescription.PATH);
        final Properties properties = new Properties();
        if (testDescription != null && testDescription.exists()) {
            try (InputStream in = testDescription.openStream()) {
                properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return new TestDescription(properties);
    }

    /**
     * Creates a test description based on the test deployment and attaches the description to the deployment for later
     * usage. For EAR's this attaches the configuration to each module in the EAR.
     *
     * @param testDeployment the test deployment to gather information from
     */
    public static void addTestDescription(final TestDeployment testDeployment) {
        String targetContainer = null;
        String arqDeploymentName = null;
        if (testDeployment.getTargetDescription() != null) {
            targetContainer = testDeployment.getTargetDescription().getName();
        }
        if (testDeployment.getDeploymentName() != null) {
            arqDeploymentName = testDeployment.getDeploymentName();
        }
        final Archive<?> archive = testDeployment.getApplicationArchive();
        if (archive instanceof EnterpriseArchive) {
            // We need to update EAR's modules separately
            final EnterpriseArchive ear = (EnterpriseArchive) archive;
            final Map<ArchivePath, Node> modules = ear
                    .getContent(ROOT_FILTER);
            for (Node module : modules.values()) {
                if (module.getAsset() instanceof ArchiveAsset) {
                    final Archive<?> moduleArchive = ((ArchiveAsset) module.getAsset()).getArchive();
                    addTestDescription(moduleArchive, targetContainer, arqDeploymentName);
                }
            }
        }
        // Always add a description for the current archive
        addTestDescription(archive, targetContainer, arqDeploymentName);
    }

    /**
     * The container the test and deployment target.
     *
     * @return the optional name of the target container for the test
     */
    public Optional<String> targetContainer() {
        return Optional.ofNullable(properties.getProperty(TARGET_CONTAINER));
    }

    /**
     * The deployments name. This will be the name of the deployed archive.
     *
     * @return the deployments name
     */
    public String deploymentName() {
        return properties.getProperty(DEPLOYMENT_NAME);
    }

    /**
     * The name of the deployment relevant to Arquillian. This is the value from {@link Deployment#name()}
     *
     * @return the optional name of the arquillian deployment
     */
    public Optional<String> arquillianDeploymentName() {
        return Optional.ofNullable(properties.getProperty(ARQ_DEPLOYMENT_NAME));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TestDescription)) {
            return false;
        }

        final TestDescription other = (TestDescription) o;
        return Objects.equals(targetContainer(), other.targetContainer())
                && Objects.equals(deploymentName(), other.deploymentName())
                && Objects.equals(arquillianDeploymentName(), other.arquillianDeploymentName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetContainer(), deploymentName(), arquillianDeploymentName());
    }

    @Override
    public String toString() {
        return "TestDescription [targetContainer=" + targetContainer() + ", deploymentName=" + deploymentName()
                + ", arquillianDeploymentName=" + arquillianDeploymentName() + "]";
    }

    private static void addTestDescription(final Archive<?> archive, final String targetContainer,
            final String arqDeploymentName) {
        try {
            final Properties properties = new Properties();
            if (archive.contains(TestDescription.PATH)) {
                try (InputStream in = archive.delete(TestDescription.PATH).getAsset().openStream()) {
                    properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
                }
            }
            if (targetContainer != null) {
                properties.put(TestDescription.TARGET_CONTAINER, targetContainer);
            }
            if (arqDeploymentName != null) {
                properties.put(TestDescription.ARQ_DEPLOYMENT_NAME, arqDeploymentName);
            }
            properties.put(TestDescription.DEPLOYMENT_NAME, archive.getName());
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                properties.store(out, null);
                archive.add(new ByteArrayAsset(out.toByteArray()), TestDescription.PATH);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

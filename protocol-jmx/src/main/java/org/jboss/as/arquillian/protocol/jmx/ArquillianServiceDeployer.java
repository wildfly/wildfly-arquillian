/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.protocol.jmx;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.context.annotation.ContainerScoped;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.arquillian.container.NetworkUtils;
import org.jboss.as.arquillian.protocol.jmx.ExtendedJMXProtocol.ServiceArchiveHolder;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.plugin.tools.ContainerDescription;

/**
 * A deployer for the Arquillian JMXProtocol endpoint.
 *
 * @author thomas.diesler@jboss.com
 * @see JMXProtocolPackager
 * @since 31-May-2011
 */
public class ArquillianServiceDeployer {

    @Inject
    @ContainerScoped
    private Instance<ManagementClient> managementClientInstance;

    private static final Logger log = Logger.getLogger(ArquillianServiceDeployer.class);

    private Set<String> serviceArchiveDeployed = new HashSet<String>();

    public synchronized void doServiceDeploy(@Observes(precedence = 1) BeforeDeploy event, Container container,
            ServiceArchiveHolder archiveHolder) {
        // already deployed?
        if (serviceArchiveDeployed.contains(container.getName())) {
            archiveHolder.deploymentExistsAndRemove(event.getDeployment().getName()); // cleanup
            return;
        }

        // only deploy the service if the deployment has been enriched by the jmx-as7 protocol
        if (archiveHolder.deploymentExistsAndRemove(event.getDeployment().getName())) {
            try {
                final ManagementClient client = managementClientInstance.get();
                // As of WildFly Arquillian 3.0.0 a minimum of WildFly 13 or JBoss EAP 7.2 is required. This is due to the
                // WFARQ-50 changes which use the new MSC service API's. The model version of this is 7.0.0 so it's best to
                // test that as WildFly 13 is at 7.0.0 and EAP 7.2 is at 8.0.0. Also the product-version may be null.
                final ContainerDescription containerDescription = ContainerDescription.lookup(client.getControllerClient());
                if (containerDescription.getModelVersion().major() < 7) {
                    String productName = containerDescription.getProductName();
                    if (productName == null) {
                        productName = "WildFly";
                    }
                    final String productVersion = containerDescription.getProductVersion();
                    final StringBuilder msg = new StringBuilder(64)
                            .append(productName);
                    if (productVersion != null) {
                        msg.append(' ').append(productVersion);
                    }
                    msg.append(" does not meet the minimum required version");
                    if (productName.contains("WildFly")) {
                        msg.append(" of 13.0.0.Final.");
                    } else if (productName.contains("EAP")) {
                        msg.append(" of 7.2.0.GA.");
                    } else {
                        msg.append('.');
                    }
                    throw new RuntimeException(msg.toString());
                }
            } catch (IOException e) {
                log.error("Failed to determine the version of the running container.", e);
            }
            JavaArchive serviceArchive = (JavaArchive) archiveHolder.getArchive();
            try {
                log.infof("Deploy arquillian service: %s", serviceArchive);
                final Map<String, String> props = container.getContainerConfiguration().getContainerProperties();
                // MASSIVE HACK
                // write the management connection props to the archive, so we can access them from the server
                final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bytes);
                out.writeObject(props.get("managementPort"));
                out.writeObject(NetworkUtils.formatPossibleIpv6Address(props.get("managementAddress")));
                out.writeObject(NetworkUtils.formatPossibleIpv6Address(props.get("managementProtocol")));
                out.writeObject(props.get("authenticationConfig"));
                out.close();
                serviceArchive.addAsManifestResource(new ByteArrayAsset(bytes.toByteArray()),
                        "org.jboss.as.managementConnectionProps");

                DeployableContainer<?> deployableContainer = container.getDeployableContainer();
                deployableContainer.deploy(serviceArchive);
                serviceArchiveDeployed.add(container.getName());
            } catch (Throwable th) {
                log.error("Cannot deploy arquillian service", th);
            }
        }
    }

    public synchronized void undeploy(@Observes BeforeStop event, Container container, ServiceArchiveHolder archiveHolder) {
        // clean up if we deployed to this container?
        if (serviceArchiveDeployed.contains(container.getName())) {
            try {
                Archive<?> serviceArchive = archiveHolder.getArchive();
                log.infof("Undeploy arquillian service: %s", serviceArchive);
                DeployableContainer<?> deployableContainer = container.getDeployableContainer();
                deployableContainer.undeploy(serviceArchive);
                serviceArchiveDeployed.remove(container.getName());
            } catch (Throwable th) {
                log.error("Cannot undeploy arquillian service", th);
            }
        }
    }
}

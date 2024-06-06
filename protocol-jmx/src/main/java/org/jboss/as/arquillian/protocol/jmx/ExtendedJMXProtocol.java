/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.protocol.jmx;

import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.protocol.jmx.AbstractJMXProtocol;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.jboss.shrinkwrap.api.Archive;

/**
 * The JBossAS JMXProtocol extension.
 *
 * @author thomas.diesler@jboss.com
 * @since 31-May-2011
 */
public class ExtendedJMXProtocol extends AbstractJMXProtocol<ExtendedJMXProtocolConfiguration> {

    @Inject
    @SuiteScoped
    private InstanceProducer<ServiceArchiveHolder> archiveHolderInst;

    public Class<ExtendedJMXProtocolConfiguration> getProtocolConfigurationClass() {
        return ExtendedJMXProtocolConfiguration.class;
    }

    @Override
    public DeploymentPackager getPackager() {
        if (archiveHolderInst.get() == null) {
            archiveHolderInst.set(new ServiceArchiveHolder());
        }
        return new JMXProtocolPackager(archiveHolderInst.get());
    }

    @Override
    public String getProtocolName() {
        return "jmx-as7";
    }

    class ServiceArchiveHolder {
        /*
         * We store the Arquillian Service so we only create it once. It is later deployed on first Deployment that needs it.
         */
        private Archive<?> serviceArchive;

        /*
         * Hold the Archives that have been enriched with the jmx-as7 protocol so we can deploy the serviceArchive.
         * This is removed in ArquillianServiceDeployer.
         */
        private Set<String> preparedDeployments = new HashSet<String>();

        Archive<?> getArchive() {
            return serviceArchive;
        }

        void setArchive(Archive<?> serviceArchive) {
            this.serviceArchive = serviceArchive;
        }

        void addPreparedDeployment(String deploymentName) {
            if (deploymentName != null) {
                preparedDeployments.add(deploymentName);
            }
        }

        public boolean deploymentExistsAndRemove(String deploymentName) {
            if (deploymentName != null) {
                return preparedDeployments.remove(deploymentName);
            }
            return false;
        }
    }
}

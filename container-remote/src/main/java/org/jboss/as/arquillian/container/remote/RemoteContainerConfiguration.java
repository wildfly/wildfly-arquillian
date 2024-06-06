/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.remote;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.as.arquillian.container.CommonContainerConfiguration;

/**
 * JBossAsManagedConfiguration
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class RemoteContainerConfiguration extends CommonContainerConfiguration {

    @Override
    public void validate() throws ConfigurationException {
        super.validate();
    }
}
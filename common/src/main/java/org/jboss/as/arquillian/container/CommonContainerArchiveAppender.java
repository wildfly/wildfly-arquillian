/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2024 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.as.arquillian.container;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.plugin.tools.OperationExecutionException;

/**
 * Creates a library to add to deployments for common container based dependencies for in-container tests.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class CommonContainerArchiveAppender implements AuxiliaryArchiveAppender {

    @Override
    public Archive<?> createAuxiliaryArchive() {
        return ShrinkWrap.create(JavaArchive.class, "wildfly-common-testencricher.jar")
                // These two types are added to avoid exceptions with class loading for in-container tests. These
                // shouldn't really be used for in-container tests.
                .addClasses(ServerSetupTask.class, ServerSetup.class)
                .addClasses(ManagementClient.class)
                // Adds wildfly-plugin-tools, this exception itself is explicitly needed
                .addPackages(true, OperationExecutionException.class.getPackage())
                .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                        + "Dependencies: org.jboss.as.controller-client,org.jboss.dmr\n"));
    }
}

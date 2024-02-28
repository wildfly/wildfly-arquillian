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

package org.wildfly.arquillian.junit.enricher;

import org.jboss.arquillian.container.test.spi.client.deployment.AuxiliaryArchiveAppender;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.arquillian.junit.annotations.JBossHome;
import org.wildfly.arquillian.junit.condition.JBossHomeParameterResolver;
import org.wildfly.plugin.tools.VersionComparator;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ExtensionAuxiliaryArchiveAppender implements AuxiliaryArchiveAppender {
    @Override
    public Archive<?> createAuxiliaryArchive() {
        return ShrinkWrap.create(JavaArchive.class, "wildfly-junit-utils.jar")
                .addPackage(JBossHome.class.getPackage())
                .addPackage(JBossHomeParameterResolver.class.getPackage())
                .addClass(VersionComparator.class);
    }
}

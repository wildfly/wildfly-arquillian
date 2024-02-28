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

package org.wildfly.arquillian.junit.jboss.home;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.wildfly.arquillian.junit.annotations.JBossHome;

/**
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Tag("env.var")
@Tag("system.property")
public class JBossHomeParameterTestCase {

    @Test
    public void stringSet(@JBossHome final String value) {
        Assertions.assertNotNull(value, "Expected the JBossHome to be set to a java.lang.String");
    }

    @Test
    public void pathSet(@JBossHome final Path value) {
        Assertions.assertNotNull(value, "Expected the JBossHome to be set to a java.nio.file.Path");
    }

    @Test
    public void fileSet(@JBossHome final File value) {
        Assertions.assertNotNull(value, "Expected the JBossHome to be set to a java.io.File");
    }
}

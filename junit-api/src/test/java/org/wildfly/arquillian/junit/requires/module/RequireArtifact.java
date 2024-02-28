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

package org.wildfly.arquillian.junit.requires.module;

import org.junit.jupiter.api.Test;
import org.wildfly.arquillian.junit.annotations.RequiresModule;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("NewClassNamingConvention")
@RequiresModule("org.wildfly.arquillian.junit.test.artifact")
public class RequireArtifact {

    @Test
    public void passing() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.arquillian.junit.test.artifact", minVersion = "2.0.0")
    public void skippedVersion() {
    }

    @Test
    @RequiresModule(value = "org.wildfly.arquillian.junit.test.artifact.invalid")
    public void skippedMissingModule() {
    }
}

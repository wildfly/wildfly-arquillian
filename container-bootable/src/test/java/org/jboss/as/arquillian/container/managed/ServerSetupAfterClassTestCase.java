/*
 * Copyright 2017 Red Hat, Inc.
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

package org.jboss.as.arquillian.container.managed;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunWith(Arquillian.class)
@RunAsClient
public class ServerSetupAfterClassTestCase extends TestOperations {
    @ArquillianResource
    private ManagementClient client;

    @Deployment
    public static JavaArchive deployment() {
        // Create a dummy deployment so the client can be injected
        return ShrinkWrap.create(JavaArchive.class).addManifest();
    }

    @Test
    public void testSystemPropertyRemoved() throws Exception {
        // All deployments from the ServerSetupDeploymentTestCase should have been undeployed and the
        // ServerSetupTask.tearDown() should have been invoked
        testSystemProperty(ServerSetupTestSuite.SYSTEM_PROPERTY_KEY);
    }

    @Override
    ManagementClient getClient() {
        return client;
    }
}

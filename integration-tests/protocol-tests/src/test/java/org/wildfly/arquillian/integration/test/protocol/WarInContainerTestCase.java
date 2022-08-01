/*
 * Copyright 2022 Red Hat, Inc.
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

package org.wildfly.arquillian.integration.test.protocol;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@ExtendWith(ArquillianExtension.class)
public class WarInContainerTestCase extends AbstractInContainerTestCase {
    private static final String DEPLOYMENT_NAME = WarInContainerTestCase.class.getSimpleName() + ".war";

    @Deployment
    public static WebArchive create() {
        return ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME)
                .addClasses(Protocol.class,
                        // Not fully used, but needed for the deployment to not log errors
                        AbstractInContainerTestCase.class,
                        ServerSetupTask.class
                )
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}

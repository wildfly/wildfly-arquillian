/*
 * Copyright 2015 Red Hat, Inc.
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
package org.jboss.arquillian.testenricher.msc;

import org.jboss.arquillian.container.test.spi.RemoteLoadableExtension;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;

/**
 * MSCEnricherExtension
 *
 * @author Thomas.Diesler@jboss.com
 * @since 07-Jun-2011
 */
public class MSCEnricherRemoteExtension implements RemoteLoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        // Don't load the MSCTestEnricher unless the MSC classes can be found at runtime
        if (Validate.classExists("org.jboss.msc.service.ServiceContainer")) {
            builder.service(ResourceProvider.class, ServiceContainerProvider.class);
            builder.service(ResourceProvider.class, ServiceTargetProvider.class);
        }
    }

}

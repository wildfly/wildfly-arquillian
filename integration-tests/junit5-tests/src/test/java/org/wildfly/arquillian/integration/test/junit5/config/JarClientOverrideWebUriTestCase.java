/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.junit5.config;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class JarClientOverrideWebUriTestCase extends AbstractOverrideWebUriTest {

    @Deployment(testable = false)
    public static JavaArchive deployment() {
        return ShrinkWrap.create(JavaArchive.class, JarClientOverrideWebUriTestCase.class.getSimpleName() + ".war")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
}

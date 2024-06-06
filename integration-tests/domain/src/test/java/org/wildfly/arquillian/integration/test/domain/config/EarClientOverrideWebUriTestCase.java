/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.arquillian.integration.test.domain.config;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.wildfly.arquillian.domain.api.TargetsServerGroup;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class EarClientOverrideWebUriTestCase extends AbstractOverrideWebUriTest {

    @Deployment(testable = false)
    @TargetsServerGroup("main-server-group")
    public static EnterpriseArchive deployment() {
        return ShrinkWrap.create(EnterpriseArchive.class, EarClientOverrideWebUriTestCase.class.getSimpleName() + ".ear")
                .addAsModule(
                        ShrinkWrap.create(WebArchive.class, EarClientOverrideWebUriTestCase.class.getSimpleName() + "-war.war")
                                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml"));
    }
}

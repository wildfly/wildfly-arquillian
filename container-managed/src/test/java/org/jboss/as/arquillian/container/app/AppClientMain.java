/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.app;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;

import org.jboss.logging.Logger;

public class AppClientMain {
    private static final Logger logger = Logger.getLogger("org.jboss.as.test.appclient");

    @Resource(lookup = "java:comp/InAppClientContainer")
    private static boolean appclient;

    @EJB
    private static EjbBusiness appClientSingletonRemote;

    public static void main(final String[] params) {
        logger.info("AppClientMain.begin");

        if (!appclient) {
            logger.error("InAppClientContainer was not true, FAILED");
            throw new RuntimeException("InAppClientContainer was not true");
        }

        try {
            String result = appClientSingletonRemote.clientCall(params[0]);
            logger.info("AppClientMain.RESULT: " + result);
            logger.info("AppClientMain.SUCCESS");
        } catch (Exception e) {
            logger.error("AppClientMain.FAILED", e);
        }
        logger.info("AppClientMain.end");
    }

}

/*
 * Copyright 2023 Red Hat, Inc.
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

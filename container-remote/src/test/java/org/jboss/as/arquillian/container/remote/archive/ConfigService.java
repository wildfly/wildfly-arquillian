/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.as.arquillian.container.remote.archive;

import org.jboss.logging.Logger;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class ConfigService implements ConfigServiceMBean {

    Logger log = Logger.getLogger(ConfigService.class);

    private int interval;

    public int getIntervalSeconds() {
        return interval;
    }

    public void setIntervalSeconds(int interval) {
        log.info("Setting IntervalSeconds to " + interval);
        this.interval = interval;
    }
}

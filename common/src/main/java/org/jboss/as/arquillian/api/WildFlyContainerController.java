/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.arquillian.api;

import org.jboss.arquillian.container.test.api.ContainerController;

/**
 * {@inheritDoc}
 * <p/>
 * This extension to the original controller provides WildFly-specific lifecycle control methods.
 *
 * @author Radoslav Husar
 * @version Jan 2015
 */
public interface WildFlyContainerController extends ContainerController {

    /**
     * Stops the given container with a timeout; corresponds to {@code :shutdown(timeout=Y)} management operation.
     * <strong>Only compatible with WildFly 9 and newer!</strong>
     *
     * @param containerQualifier container qualifier
     * @param timeout            timeout in seconds to wait during suspend phase
     */
    void stop(String containerQualifier, int timeout);

}

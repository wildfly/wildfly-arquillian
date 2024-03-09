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

package org.jboss.as.arquillian.api;

import org.jboss.as.arquillian.container.ManagementClient;

/**
 *
 * A task which is run before deployment that allows the client to customize the server config.
 *
 * @author Stuart Douglas
 */
public interface ServerSetupTask {

    /**
     * Execute any necessary setup work that needs to happen before the first deployment
     * to the given container.
     * <p>
     * <strong>Note on exception handling:</strong> If an implementation of this method
     * throws {@code org.junit.AssumptionViolatedException}, the implementation can assume
     * the following:
     * <ol>
     * <li>Any subsequent {@code ServerSetupTask}s {@link ServerSetup associated with test class}
     * <strong>will not</strong> be executed.</li>
     * <li>The deployment event that triggered the call to this method will be skipped.</li>
     * <li>The {@link #tearDown(ManagementClient, String) tearDown} method of the instance
     * that threw the exception <strong>will not</strong> be invoked. Therefore, implementations
     * that throw {@code AssumptionViolatedException} should do so before altering any
     * system state.</li>
     * <li>The {@link #tearDown(ManagementClient, String) tearDown} method for any
     * previously executed {@code ServerSetupTask}s {@link ServerSetup associated with test class}
     * <strong>will</strong> be invoked.</li>
     * </ol>
     *
     * @param managementClient management client to use to interact with the container
     * @param containerId      id of the container to which the deployment will be deployed
     * @throws Exception if a failure occurs
     */
    void setup(ManagementClient managementClient, String containerId) throws Exception;

    /**
     * Execute any tear down work that needs to happen after the last deployment associated
     * with the given container has been undeployed.
     *
     * @param managementClient management client to use to interact with the container
     * @param containerId      id of the container to which the deployment will be deployed
     * @throws Exception if a failure occurs
     */
    void tearDown(ManagementClient managementClient, String containerId) throws Exception;
}

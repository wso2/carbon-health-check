/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.healthcheck.api.core;

import org.wso2.carbon.healthcheck.api.core.exception.BadHealthException;
import org.wso2.carbon.healthcheck.api.core.model.HealthCheckerConfig;

import java.util.Properties;

/**
 * Health Checker Interface which is the API for a health checker. A Carbon Health Checker implementation should
 * implement this API and should be registered as an OSGI service in order to be picked up for health check execution.
 */
public interface HealthChecker {

    /**
     * A unique name for the health checker.
     *
     * @return The name of the health checker implementation.
     */
    String getName();

    /**
     * Get the execution order of a health checker. Health checkers are executed based on this order.
     *
     * @return The execution order of Health Checker.
     */
    int getOrder();

    /**
     * To check whether the health checker is enabled or not.
     *
     * @return Whether the health checker is enabled or not.
     */
    boolean isEnabled();

    /**
     * Initialize health checkers by passing required configurations.
     *
     * @param healthCheckerConfig Health checker configurations.
     */
    void init(HealthCheckerConfig healthCheckerConfig);

    /**
     * Check Health. The implementation will check respective health parameters and execute logic to evaluate health.
     *
     * @return Properties which needs to be shown in the response.
     * @throws BadHealthException If the health check fails a BadHealthException will be thrown which consists of a
     *                            List of errors
     */
    Properties checkHealth() throws BadHealthException;

}

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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.healthcheck.api.core.exception.BadHealthException;
import org.wso2.carbon.healthcheck.api.core.exception.HealthCheckError;
import org.wso2.carbon.healthcheck.api.core.exception.HealthCheckServiceException;
import org.wso2.carbon.healthcheck.api.core.internal.HealthMonitorServiceDataHolder;
import org.wso2.carbon.healthcheck.api.core.util.HealthCheckConfigParser;
import org.wso2.carbon.healthcheck.api.core.util.LambdaExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Service which tests the health of carbon instance.
 */
public class CarbonHealthCheckService {

    private static final Log log = LogFactory.getLog(CarbonHealthCheckService.class);
    private static CarbonHealthCheckService instance = new CarbonHealthCheckService();

    /**
     * To make it singleton
     */
    private CarbonHealthCheckService() {

    }

    public static CarbonHealthCheckService getInstance() {

        return instance;
    }

    /**
     * @return Properties which are returned by HealthChecker implementation.
     * @throws HealthCheckServiceException In a case if Heath check failure present, a HealthCheckServiceException
     *                                     will be thrown.
     */
    public Properties healthCheck() throws HealthCheckServiceException {

        Properties cumulativeProperties = new Properties();
        // Return if the service is not enabled.
        if (!HealthCheckConfigParser.getInstance().isHealthCheckServiceEnabled()) {
            if (log.isDebugEnabled()) {
                log.debug("Health check service is not enabled. Hence returning without processing");
            }
            return cumulativeProperties;
        }

        List<HealthCheckError> healthCheckErrors = executeHealthCheckers(cumulativeProperties);

        if (!healthCheckErrors.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Health Check failed. " + healthCheckErrors.size() + " number of errors are found");
            }
            throw new HealthCheckServiceException("Health Check Failed", healthCheckErrors);
        }

        if (log.isDebugEnabled()) {
            log.debug("Returning results from health check service : ");
            cumulativeProperties.forEach((key, value) -> log.debug(key + ": " + value));
            log.debug("=============End of Properties=============");
        }
        return cumulativeProperties;
    }

    private List<HealthCheckError> executeHealthCheckers(Properties cumulativeProperties) {

        List<HealthChecker> healthCheckers = HealthMonitorServiceDataHolder.getInstance().getHealthCheckers();
        List<HealthCheckError> errors = new ArrayList<>();
        healthCheckers.forEach(LambdaExceptionUtils.rethrowConsumer(healthChecker -> {

            if (healthChecker != null && healthChecker.isEnabled()) {
                if (log.isDebugEnabled()) {
                    log.debug("Executing health checker: " + healthChecker.getName() + " with order: " +
                            healthChecker.getOrder());
                }
                try {
                    Properties checkerProperties = healthChecker.checkHealth();
                    cumulativeProperties.putAll(checkerProperties);
                } catch (BadHealthException e) {
                    handleError(errors, healthChecker, e);
                }
            }
        }));
        return errors;
    }

    private void handleError(List<HealthCheckError> errors, HealthChecker healthChecker, BadHealthException e) {

        List<HealthCheckError> returnedErrors = e.getErrors();
        if (log.isDebugEnabled()) {
            log.debug("Error while executing health checker: " + healthChecker.getName(), e);
        }
        errors.addAll(returnedErrors);
        if (StringUtils.isNotEmpty(e.getErrorCode())) {
            String errorDescription = null;
            if (e.getCause() != null) {
                errorDescription = e.getCause().getMessage();
            }
            errors.add(new HealthCheckError(e.getErrorCode(), e.getMessage(), errorDescription));
        }
    }
}

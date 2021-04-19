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

package org.wso2.carbon.healthcheck.api.core.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.healthcheck.api.core.AbstractHealthChecker;
import org.wso2.carbon.healthcheck.api.core.Constants;
import org.wso2.carbon.healthcheck.api.core.exception.BadHealthException;
import org.wso2.carbon.healthcheck.api.core.exception.HealthCheckError;
import org.wso2.carbon.healthcheck.api.core.model.HealthCheckerConfig;

/**
 * This HealthChecker will check the health of the Heap Memory to validate if 
 * platform if the error Out Of Memory happened checking the Heap Memory through the MemoryMXBean
 */
public class OOMHealthChecker extends AbstractHealthChecker {

    private static final Log log = LogFactory.getLog(OOMHealthChecker.class);
    private static final String OOM_HEALTH_CHECKER = "OOMHealthChecker";
    private static final String OOM_PROP = "is.out.of.memory";

    @Override
    public void init(HealthCheckerConfig healthCheckerConfig) {
        super.init(healthCheckerConfig);
    }

    @Override
    public String getName() {
        return OOM_HEALTH_CHECKER;
    }

    @Override
    public Properties checkHealth() throws BadHealthException {
        return isOOM();
    }
    
    private Properties isOOM() throws BadHealthException {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
		
		if (heapMemoryUsage.getMax() == heapMemoryUsage.getCommitted()) {
			List<HealthCheckError> errors = new ArrayList<>();
			errors.add(new HealthCheckError(Constants.ErrorCodes.ERROR_OOM,
                    "The server is out of memory", "java.lang.OutOfMemoryError: Java heap space"));
			BadHealthException badHealthException = new BadHealthException("Out Of Memory Health Check", errors);
			log.error(badHealthException);
			throw badHealthException;
		}
		
		Properties cumulativeResults = new Properties();
		cumulativeResults.setProperty(OOM_PROP, "false");
		return cumulativeResults;
	}
}

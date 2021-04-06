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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.DataSourceProxy;
import org.wso2.carbon.healthcheck.api.core.AbstractHealthChecker;
import org.wso2.carbon.healthcheck.api.core.Constants;
import org.wso2.carbon.healthcheck.api.core.exception.BadHealthException;
import org.wso2.carbon.healthcheck.api.core.exception.HealthCheckError;
import org.wso2.carbon.healthcheck.api.core.exception.HealthCheckFailedException;
import org.wso2.carbon.healthcheck.api.core.model.HealthCheckerConfig;
import org.wso2.carbon.healthcheck.api.core.util.LambdaExceptionUtils;
import org.wso2.carbon.healthcheck.api.core.util.Utils;
import org.wso2.carbon.ndatasource.common.DataSourceException;
import org.wso2.carbon.ndatasource.core.CarbonDataSource;
import org.wso2.carbon.ndatasource.core.DataSourceManager;
import org.wso2.carbon.ndatasource.core.JNDIConfig;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * This HealthChecker will check the health of data sources which are configured in master-datasources by checking
 * the connection pool information and by getting a connection from the pool.
 */
public class OOMHealthChecker extends AbstractHealthChecker {

    private static final Log log = LogFactory.getLog(OOMHealthChecker.class);
    private static final String OOM_HEALTH_CHECKER = "OOMHealthChecker";
    private static final String OOM_PROP = "is.out.of.memory";
    public static final String ERROR_OOM = "OOM_00001";

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
        List<HealthCheckError> errors = new ArrayList<>();
        Properties cumulativeResults = new Properties();
        isOOM(cumulativeResults);
        return cumulativeResults;
    }
    
    private void isOOM(Properties cumulativeResults) throws BadHealthException {
		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
		
		if (heapMemoryUsage.getMax() == heapMemoryUsage.getCommitted()) {
			cumulativeResults.setProperty(OOM_PROP, "true");
			List<HealthCheckError> errors = new ArrayList<>();
			errors.add(new HealthCheckError(ERROR_OOM,
                    "The server is out of memory", "java.lang.OutOfMemoryError: Java heap space"));
			throw new BadHealthException("Out Of Memory Health Check", errors);
		} else {
			cumulativeResults.setProperty(OOM_PROP, "false");
		}
		
	}


}

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

import java.lang.management.ManagementFactory;
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
public class DataSourceHealthChecker extends AbstractHealthChecker {

    private static final Log log = LogFactory.getLog(DataSourceHealthChecker.class);
    private static final String DATA_SOURCE_HEALTH_CHECKER = "DataSourceHealthChecker";
    private static final String CONNECTIVITY_TIME_IN_MS = ".connectivity.time.ms";
    private static final String MONITORED_DATASOURCES = "monitored.datasources";
    private static final String POOL_USAGE_LIMIT_PERCENTAGE = "pool.usage.limit.percentage";
    private static final String DATASOURCE_MBEAN_QUERY_FILTER = "*:type=DataSource";
    private static final String ACTIVE_CONNECTION_COUNT_KEY = ".active.connection.count";
    private Integer poolHealthyPercentage = 80;
    private List<String> monitoredDataSoruces = new ArrayList<>();
    private boolean monitorAllDataSources = true;

    @Override
    public void init(HealthCheckerConfig healthCheckerConfig) {

        super.init(healthCheckerConfig);
        setHealthyPoolLimit(healthCheckerConfig);
        initMonitoredDataSourcesList(healthCheckerConfig);
    }

    @Override
    public String getName() {

        return DATA_SOURCE_HEALTH_CHECKER;
    }

    @Override
    public Properties checkHealth() throws BadHealthException {

        List<HealthCheckError> errors = new ArrayList<>();
        Properties cumilativeResults = new Properties();
        Properties connectivityProperties;
        try {
            connectivityProperties = testDBConnectivity(errors);
        } catch (HealthCheckFailedException e) {
            throw new BadHealthException(Constants.ErrorCodes.ERROR_DATA_SOURCE_CONNECTIVITY,
                    "Error while checking DB connectivity", errors, e);
        }
        cumilativeResults.putAll(connectivityProperties);
        if (!errors.isEmpty()) {
            throw new BadHealthException("Bad health in data sources", errors);
        }
        return cumilativeResults;

    }

    protected void initMonitoredDataSourcesList(HealthCheckerConfig healthCheckerConfig) {

        Object datasourcesObj = healthCheckerConfig.getProperties().get(MONITORED_DATASOURCES);
        if (datasourcesObj != null) {
            String[] datasorucesArray = datasourcesObj.toString().split(",");
            monitoredDataSoruces = Arrays.asList(datasorucesArray);
            monitorAllDataSources = false;
        }
    }

    protected void setHealthyPoolLimit(HealthCheckerConfig healthCheckerConfig) {

        String poolHealthyPercentageString =
                String.valueOf(healthCheckerConfig.getProperties().get(POOL_USAGE_LIMIT_PERCENTAGE));
        try {
            poolHealthyPercentage = Integer.parseInt(poolHealthyPercentageString);
        } catch (NumberFormatException e) {
            log.info("No integer configured for" + POOL_USAGE_LIMIT_PERCENTAGE + ", configured value is: " +
                    poolHealthyPercentageString + ", Hence defaulting to 80");
        }
    }

    protected Properties validateDataSource(DataSource dataSource, String datasourceName, List<HealthCheckError>
            errors) {

        Properties properties = new Properties();

        if (dataSource instanceof DataSourceProxy) {
            DataSourceProxy dataSourceProxy = (DataSourceProxy) dataSource;
            ConnectionPool pool = dataSourceProxy.getPool();
            int activeCount = pool.getActive();
            int maxActive = dataSourceProxy.getMaxActive();
            properties.put(datasourceName + ACTIVE_CONNECTION_COUNT_KEY, activeCount);
            validateAvailableResourceMargin(activeCount, maxActive, datasourceName, errors);
        }

        return properties;
    }

    protected void validateAvailableResourceMargin(Integer activeCount, Integer maxActiveCount,
                                                   String datasorceName, List<HealthCheckError> errors) {

        if (activeCount != null && maxActiveCount != null) {
            if (activeCount > (maxActiveCount * poolHealthyPercentage / 100)) {
                if (log.isDebugEnabled()) {
                    log.debug("Datasource: " + datasorceName + " surpasses the healthy connection percentage of " +
                            poolHealthyPercentage + ". Current usage is: " + activeCount);
                }
                errors.add(new HealthCheckError(Constants.ErrorCodes.ERROR_CONNECTION_COUNT_EXCEEDS_LIMIT,
                        "Active count of db connections exceeds the " +
                                "given limit. Active count: " + activeCount + " for datasource: " + datasorceName));
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Data source: " + datasorceName + " has a healthy pool usage of : " + activeCount);
                }
            }
        }
    }

    protected Object getMbeanAttribute(String objectName, String attributeName) throws HealthCheckFailedException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving attribute: " + attributeName + ", From Mbean: " + objectName);
        }

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName mbeanName;
        try {
            mbeanName = new ObjectName(objectName);
        } catch (MalformedObjectNameException e) {
            throw new HealthCheckFailedException("No MBeans are registered with object name: " + objectName, e);
        }
        try {
            return mbs.getAttribute(mbeanName, attributeName);
        } catch (MBeanException | AttributeNotFoundException | InstanceNotFoundException | ReflectionException e) {
            throw new HealthCheckFailedException("Error while reading attribute: " + attributeName + ", " +
                    "from object : " + objectName, e);
        }
    }

    protected Integer getNumber(Object object) {

        if (object != null) {
            return Integer.parseInt(object.toString());
        } else {
            return null;
        }
    }

    protected List<String> queryMbeans(String filter, List<HealthCheckError> errors) throws HealthCheckFailedException {

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        List<String> mBeanNames = new ArrayList<>();
        try {
            ObjectName objectName = new ObjectName(filter);
            Set<ObjectInstance> instances = server.queryMBeans(objectName, null);
            instances.forEach(objectInstance -> {

                if (log.isDebugEnabled()) {
                    log.debug("Class Name:t" + objectInstance.getClassName());
                    log.debug("Object Name:t" + objectInstance.getObjectName());
                }
                mBeanNames.add(objectInstance.getObjectName().toString());
            });
        } catch (MalformedObjectNameException e) {
            throw new HealthCheckFailedException("Error while listing data sources MBeans", e);
        }
        return mBeanNames;
    }

    protected Properties testDBConnectivity(List<HealthCheckError> errors) throws HealthCheckFailedException {

        Properties cumulativeProperties = new Properties();

        try {
            Collection<CarbonDataSource> allDataSources = DataSourceManager.getInstance().
                    getDataSourceRepository().getAllDataSources();
            Context ctx;
            try {
                ctx = new InitialContext();
            } catch (NamingException e) {
                throw new HealthCheckFailedException("Error while retrieving initial context", e);
            }

            Context finalCtx = ctx;
            allDataSources.forEach(LambdaExceptionUtils.rethrowConsumer(datasource -> {

                Properties connectivityProperties = new Properties();
                String name = datasource.getDSMInfo().getJndiConfig().getName();
                // Return without processing if this datasource is not defined under monitored datasources.
                if (!isDataSourceMonitored(name)) {
                    return;
                }
                try {

                    DataSource dataSource = (DataSource) finalCtx.lookup(name);
                    long startTime = System.currentTimeMillis();

                    // Connection will not be used since it's used to validate connectivity.
                    try (Connection connection = dataSource.getConnection()) {
                        connectivityProperties.put(name + CONNECTIVITY_TIME_IN_MS,
                                System.currentTimeMillis() - startTime);
                        Properties datasourceProperties = validateDataSource(dataSource, name, errors);
                        connectivityProperties.putAll(datasourceProperties);
                    } catch (Throwable e) {
                        // Catching throwable since all types of errors must be captured.
                        if (log.isDebugEnabled()) {
                            log.debug("Error while getting database connection for " +
                                    "datasource: " + name, e);
                        }
                        errors.add(new HealthCheckError(Constants.ErrorCodes.ERROR_DATA_SOURCE_CONNECTIVITY,
                                "Error while getting database connection for " +
                                        "datasource: " + name, Utils.getRootCauseMessage(e)));
                    }

                } catch (NamingException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Error while initializing Naming context for " +
                                "datasource: " + name, e);
                    }
                    errors.add(new HealthCheckError(Constants.ErrorCodes.ERROR_DATA_SOURCE_CONNECTIVITY,
                            "Error while initializing Naming context for " +
                                    "datasource: " + name, Utils.getRootCauseMessage(e)));
                }

                cumulativeProperties.putAll(connectivityProperties);
            }));
        } catch (DataSourceException e) {
            throw new HealthCheckFailedException("Error while retrieving all datasources", e);
        }
        return cumulativeProperties;
    }

    protected boolean isDataSourceMonitored(String name) {

        return (monitoredDataSoruces.stream().anyMatch(datasourceName -> datasourceName.equalsIgnoreCase(name)) ||
                monitorAllDataSources);
    }

}

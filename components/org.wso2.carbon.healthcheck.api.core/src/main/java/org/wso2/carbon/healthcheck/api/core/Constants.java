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

/**
 * Constants which are used within Health Checker Core implementation.
 */
public class Constants {

    public static class DataSourcesJMX {

        public static final String ACTIVE = "Active";
        public static final String MAX_ACTIVE = "MaxActive";
    }

    public static class HealthCheckConfig {

        public static final String HEALTH_CHECK_CONFIG_FILE_NAME = "health-check-config.xml";
        public static final String DEFAULT_NAMESPACE = "http://wso2.org/projects/carbon/carbon.xml";
        public static final String HEALTH_CHECKERS = "HealthCheckers";
        public static final String HEALTH_CHECKER = "HealthChecker";
        public final static String NAME = "name";
        public final static String ORDER = "orderId";
        public final static String ENABLE = "enable";
        public final static String PROPERTY = "Property";
        public final static String PROPERTY_NAME = "name";
    }

    public static class MemoryUsageLoggerConfig {

        public static final int DEFAULT_INTERVAL_SECONDS = 60;
        public static final int DEFAULT_WAIT_SECONDS  = DEFAULT_INTERVAL_SECONDS * 2;
    }

    // Error codes
    public static class ErrorCodes {

        public static final String ERROR_DATA_SOURCE_CONNECTIVITY = "HC_00001";
        public static final String ERROR_CONNECTION_COUNT_EXCEEDS_LIMIT = "HC_00002";
        public static final String ERROR_USERSTORE_CONNECTIVITY_BY_IS_USER_EXISTING = "HC_00003";
        public static final String ERROR_SERVER_STATUS_NOT_RUNNING = "HC_00004";
        public static final String ERROR_LISTING_USERSTORES = "HC_00005";
    }
}

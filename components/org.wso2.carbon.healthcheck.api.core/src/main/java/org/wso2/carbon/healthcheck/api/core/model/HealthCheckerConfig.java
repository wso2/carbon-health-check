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

package org.wso2.carbon.healthcheck.api.core.model;

import java.util.Properties;

/**
 * Config model object which is used to keep health check configurations.
 */
public class HealthCheckerConfig {

    private String name;
    private int order;
    private boolean isEnable;
    private Properties properties = new Properties();

    /**
     * Constructor for health checker config model
     *
     * @param name       Name of the health checker.
     * @param order      Execution order of the health checker.
     * @param isEnable   Whether the health checker is enabled or not.
     * @param properties Set of properties configured under particular health checker.
     */
    public HealthCheckerConfig(String name, int order, boolean isEnable, Properties properties) {

        this.name = name;
        this.order = order;
        this.isEnable = isEnable;
        this.properties = properties;
    }

    public HealthCheckerConfig(String name) {

        this.name = name;
    }

    /**
     * Name of the health checker.
     *
     * @return Name of the health checker.
     */
    public String getName() {

        return name;
    }

    /**
     * Execution order of the health checker.
     *
     * @return Execution order of the health checker.
     */
    public int getOrder() {

        return order;
    }

    /**
     * Whether the health checker is enabled or not.
     *
     * @return Whether the health checker is enabled or not.
     */
    public boolean isEnable() {

        return isEnable;
    }

    /**
     * Properties configured under particular health checker.
     *
     * @return Properties configured under specific health checker.
     */
    public Properties getProperties() {

        return properties;
    }
}

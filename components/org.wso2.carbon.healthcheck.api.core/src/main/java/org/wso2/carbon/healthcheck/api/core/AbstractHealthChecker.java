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

import org.wso2.carbon.healthcheck.api.core.model.HealthCheckerConfig;

/**
 * Abstract Health Checker which has implemented core logic of initialization, isEnable and getOrder.
 */
public abstract class AbstractHealthChecker implements HealthChecker {

    protected HealthCheckerConfig healthCheckerConfig = null;

    @Override
    public void init(HealthCheckerConfig healthCheckerConfig) {

        this.healthCheckerConfig = healthCheckerConfig;
    }

    @Override
    public boolean isEnabled() {

        return this.healthCheckerConfig == null || healthCheckerConfig.isEnable();
    }

    @Override
    public int getOrder() {

        if (this.healthCheckerConfig == null) {
            return 0;
        } else {
            return healthCheckerConfig.getOrder();
        }
    }
}

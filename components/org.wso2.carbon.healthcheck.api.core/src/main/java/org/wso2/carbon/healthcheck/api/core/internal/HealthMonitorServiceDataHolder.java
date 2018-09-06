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

package org.wso2.carbon.healthcheck.api.core.internal;

import org.wso2.carbon.healthcheck.api.core.CarbonHealthCheckService;
import org.wso2.carbon.healthcheck.api.core.HealthChecker;
import org.wso2.carbon.healthcheck.api.core.HealthCheckerComparator;
import org.wso2.carbon.server.admin.common.IServerAdmin;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Keeps data and services which are acquired through OSGI.
 */
public class HealthMonitorServiceDataHolder {

    private IServerAdmin serverAdmin;
    private RealmService realmService;
    private List<HealthChecker> healthCheckers = new ArrayList<>();
    private static HealthMonitorServiceDataHolder instance = new HealthMonitorServiceDataHolder();

    public static HealthMonitorServiceDataHolder getInstance() {

        return instance;
    }

    public IServerAdmin getServerAdmin() {

        return serverAdmin;
    }

    public void setServerAdmin(IServerAdmin serverAdmin) {

        this.serverAdmin = serverAdmin;
    }

    public List<HealthChecker> getHealthCheckers() {

        return healthCheckers;
    }

    public void addHealthChecker(HealthChecker healthChecker) {

        this.healthCheckers.add(healthChecker);
        healthCheckers.sort(new HealthCheckerComparator());
    }

    public void removeHealthChecker(HealthChecker healthChecker) {

    }

    public RealmService getRealmService() {

        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }
}

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

import org.wso2.carbon.core.ServerStatus;
import org.wso2.carbon.healthcheck.api.core.AbstractHealthChecker;
import org.wso2.carbon.healthcheck.api.core.Constants;
import org.wso2.carbon.healthcheck.api.core.exception.BadHealthException;
import org.wso2.carbon.healthcheck.api.core.internal.HealthMonitorServiceDataHolder;
import org.wso2.carbon.server.admin.common.IServerAdmin;
import org.wso2.carbon.server.admin.service.ServerAdmin;

import java.util.Properties;

/**
 * This Health Checker checks the current server status using IServerAdmin service.
 */
public class ServerStartupChecker extends AbstractHealthChecker {

    private static final String SERVER_STARTUP_HEALTH_CHECKER = "ServerStartupChecker";

    @Override
    public String getName() {

        return SERVER_STARTUP_HEALTH_CHECKER;
    }

    @Override
    public Properties checkHealth() throws BadHealthException {

        try {
            IServerAdmin serverAdmin = HealthMonitorServiceDataHolder.getInstance().getServerAdmin();
            if (serverAdmin == null || !(serverAdmin instanceof ServerAdmin)) {
                throw new BadHealthException(Constants.ErrorCodes.ERROR_SERVER_STATUS_NOT_RUNNING,
                        "Server is not started up properly. Couldn't find ServerAdmin Service");
            }

            if (!ServerStatus.STATUS_RUNNING.equalsIgnoreCase(((ServerAdmin) serverAdmin).getServerStatus())) {
                throw new BadHealthException(Constants.ErrorCodes.ERROR_SERVER_STATUS_NOT_RUNNING,
                        "Server status is not : " + ServerStatus.STATUS_RUNNING + ", Found" + " : " +
                                ((ServerAdmin) serverAdmin).getServerStatus());
            }
        } catch (Exception e) {
            // getServerStatus throws Exception.
            throw new BadHealthException(Constants.ErrorCodes.ERROR_SERVER_STATUS_NOT_RUNNING,
                    "Error while getting server status", e);
        }
        return new Properties();
    }
}

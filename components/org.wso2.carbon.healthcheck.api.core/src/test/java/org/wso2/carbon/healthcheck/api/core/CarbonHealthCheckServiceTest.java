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

import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.healthcheck.api.core.exception.HealthCheckServiceException;
import org.wso2.carbon.healthcheck.api.core.impl.ServerStartupChecker;
import org.wso2.carbon.healthcheck.api.core.internal.HealthMonitorServiceDataHolder;
import org.wso2.carbon.healthcheck.api.core.model.HealthCheckerConfig;
import org.wso2.carbon.healthcheck.api.core.util.HealthCheckConfigParser;
import org.wso2.carbon.server.admin.service.ServerAdmin;

import java.nio.file.Paths;

/**
 * Test class for CarbonHealthCheckService
 */
public class CarbonHealthCheckServiceTest {

    String carbonHome = null;
    CarbonHealthCheckService service = CarbonHealthCheckService.getInstance();

    @BeforeMethod
    public void setUp() throws Exception {

        carbonHome = Paths.get(System.getProperty("user.dir"), "src", "test", "resources").toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());
    }

    @Test
    public void getCheckHealth() throws Exception {

        HealthChecker serverStartupChecker = new ServerStartupChecker();
        HealthMonitorServiceDataHolder.getInstance().addHealthChecker(serverStartupChecker);
        HealthMonitorServiceDataHolder.getInstance().setServerAdmin(null);
        try {
            service.healthCheck();
            Assert.fail();
        } catch (HealthCheckServiceException e) {
            Assert.assertTrue(e.getErrors().size() > 0);
            Assert.assertEquals(e.getErrors().get(0).getErrorCode(),
                    Constants.ErrorCodes.ERROR_SERVER_STATUS_NOT_RUNNING);
        }
    }

    @Test
    public void checkHealthServiceEnable() throws Exception {

        HealthChecker serverStartupChecker = new ServerStartupChecker();
        HealthMonitorServiceDataHolder.getInstance().addHealthChecker(serverStartupChecker);
        Assert.assertTrue(HealthCheckConfigParser.getInstance().isHealthCheckServiceEnabled());
    }

    @Test
    public void checkHealthWithRunningServerState() throws Exception {

        ServerAdmin serverAdmin = PowerMockito.mock(ServerAdmin.class);
        PowerMockito.when(serverAdmin.getServerStatus()).thenReturn("RUNNING");
        HealthChecker serverStartupChecker = new ServerStartupChecker();
        HealthMonitorServiceDataHolder.getInstance().addHealthChecker(serverStartupChecker);
        HealthMonitorServiceDataHolder.getInstance().setServerAdmin(serverAdmin);
        service.healthCheck();
    }

    @Test
    public void checkHealthServerStateWhenNotRunning() throws Exception {

        ServerAdmin serverAdmin = PowerMockito.mock(ServerAdmin.class);
        PowerMockito.when(serverAdmin.getServerStatus()).thenReturn("NOT-RUNNING");
        HealthChecker serverStartupChecker = new ServerStartupChecker();
        HealthMonitorServiceDataHolder.getInstance().addHealthChecker(serverStartupChecker);
        HealthMonitorServiceDataHolder.getInstance().setServerAdmin(serverAdmin);
        try {
            service.healthCheck();
            Assert.fail();
        } catch (HealthCheckServiceException e) {
            Assert.assertTrue(e.getErrors().size() == 1);
        }
    }

    @Test
    public void checkParsedConfigs() throws Exception {

        HealthCheckerConfig dataSourceHealthChecker =
                HealthCheckConfigParser.getInstance().getHealthCheckerConfigMap().get("DataSourceHealthChecker");
        Assert.assertNotNull(dataSourceHealthChecker);
        Assert.assertEquals(dataSourceHealthChecker.getProperties().
                getProperty("pool.usage.limit.percentage"), "90");
    }

}

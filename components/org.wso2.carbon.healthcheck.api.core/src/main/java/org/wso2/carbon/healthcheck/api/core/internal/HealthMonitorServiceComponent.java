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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.healthcheck.api.core.CarbonHealthCheckService;
import org.wso2.carbon.healthcheck.api.core.HealthChecker;
import org.wso2.carbon.healthcheck.api.core.impl.DataSourceHealthChecker;
import org.wso2.carbon.healthcheck.api.core.impl.OOMHealthChecker;
import org.wso2.carbon.healthcheck.api.core.impl.ServerStartupChecker;
import org.wso2.carbon.healthcheck.api.core.impl.SuperTenantUSHealthChecker;
import org.wso2.carbon.healthcheck.api.core.model.HealthCheckerConfig;
import org.wso2.carbon.healthcheck.api.core.util.HealthCheckConfigParser;
import org.wso2.carbon.server.admin.common.IServerAdmin;
import org.wso2.carbon.user.core.service.RealmService;

@Component(
        name = "health.check.dscomponent",
        immediate = true
)
public class HealthMonitorServiceComponent {

    private static final Log log = LogFactory.getLog(HealthMonitorServiceComponent.class);

    protected void activate(ComponentContext ctxt) {

        try {
            ctxt.getBundleContext().registerService(CarbonHealthCheckService.class.getName(),
                    CarbonHealthCheckService.getInstance(), null);
            ctxt.getBundleContext().registerService(HealthChecker.class.getName(),
                    new ServerStartupChecker(), null);
            ctxt.getBundleContext().registerService(HealthChecker.class.getName(),
                    new DataSourceHealthChecker(), null);
            ctxt.getBundleContext().registerService(HealthChecker.class.getName(),
            		new OOMHealthChecker(), null);
            ctxt.getBundleContext().registerService(HealthChecker.class.getName(), new SuperTenantUSHealthChecker(),
                    null);
            log.info("Carbon health monitoring service is activated..");
        } catch (Throwable e) {
            // Catching throwable to avoid retrying to initiate component.
            log.error("Failed to activate carbon health check bundle", e);
        }
    }

    @Reference(
            name = "health.check.dscomponent",
            service = HealthChecker.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetHealthChecker"
    )
    protected void setHealthChecker(HealthChecker healthChecker) {

        HealthCheckerConfig healthCheckerConfig = HealthCheckConfigParser.getInstance().
                getHealthCheckerConfigMap().get(healthChecker.getName());

        if (healthChecker == null && StringUtils.isEmpty(healthChecker.getName())) {
            if (log.isDebugEnabled()) {
                log.debug("Null health checkers or a health checker without name are not registered.");
            }
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Registering health checker: " + healthChecker.getName());
        }
        if (healthCheckerConfig == null) {
            healthCheckerConfig = new HealthCheckerConfig(healthChecker.getName());
        }
        healthChecker.init(healthCheckerConfig);
        HealthMonitorServiceDataHolder.getInstance().addHealthChecker(healthChecker);
    }

    protected void unsetHealthChecker(HealthChecker healthChecker) {

        HealthMonitorServiceDataHolder.getInstance().removeHealthChecker(healthChecker);
    }

    @Reference(
            name = "serveradmin.service.component",
            service = IServerAdmin.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetServerAdmin"
    )
    protected void setServerAdmin(IServerAdmin serverAdmin) {

        HealthMonitorServiceDataHolder.getInstance().setServerAdmin(serverAdmin);
    }

    protected void unsetServerAdmin(IServerAdmin serverAdmin) {

        HealthMonitorServiceDataHolder.getInstance().setServerAdmin(null);
    }

    @Reference(
            name = "user.realmservice.default",
            service = RealmService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetRealmService"
    )
    protected void setRealmService(RealmService realmService) {

        HealthMonitorServiceDataHolder.getInstance().setRealmService(realmService);
    }

    /**
     * @param realmService Realm Service.
     */
    protected void unsetRealmService(RealmService realmService) {

        HealthMonitorServiceDataHolder.getInstance().setRealmService(null);
    }

}

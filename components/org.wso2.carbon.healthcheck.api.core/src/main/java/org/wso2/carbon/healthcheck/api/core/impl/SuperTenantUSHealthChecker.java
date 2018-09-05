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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.healthcheck.api.core.AbstractHealthChecker;
import org.wso2.carbon.healthcheck.api.core.Constants;
import org.wso2.carbon.healthcheck.api.core.exception.BadHealthException;
import org.wso2.carbon.healthcheck.api.core.exception.HealthCheckError;
import org.wso2.carbon.healthcheck.api.core.internal.HealthMonitorServiceDataHolder;
import org.wso2.carbon.healthcheck.api.core.model.HealthCheckerConfig;
import org.wso2.carbon.healthcheck.api.core.util.LambdaExceptionUtils;
import org.wso2.carbon.healthcheck.api.core.util.Utils;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SuperTenantUSHealthChecker extends AbstractHealthChecker {

    private static final Log log = LogFactory.getLog(SuperTenantUSHealthChecker.class);
    private static final String MONITORED_USER_STORES = "monitored.user.stores";
    private List<String> monitoredUserStores = new ArrayList<>();
    private boolean monitorAllUserStores = true;
    private static final String DISABLED = "Disabled";
    private static final String TEST_USER = "testUser";

    @Override
    public String getName() {

        return "SuperTenantUSHealthChecker";
    }

    @Override
    public void init(HealthCheckerConfig healthCheckerConfig) {

        super.init(healthCheckerConfig);
        initMonitoredUserStores(healthCheckerConfig);
    }

    @Override
    public Properties checkHealth() throws BadHealthException {

        Properties properties = new Properties();
        List<HealthCheckError> errors = new ArrayList<>();
        RealmService realmService = HealthMonitorServiceDataHolder.getInstance().getRealmService();
        try {
            UserRealm tenantUserRealm = realmService.getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID);
            if (tenantUserRealm == null) {
                return properties;
            }
            String[] secondaryUSM = getSecondaryUSMNames(tenantUserRealm
                    .getRealmConfiguration());
            if (log.isDebugEnabled()) {
                log.debug("Available secondary user stores; " + ArrayUtils.toString(secondaryUSM));
            }
            org.wso2.carbon.user.core.UserStoreManager userStoreManager =
                    (org.wso2.carbon.user.core.UserStoreManager) tenantUserRealm
                            .getUserStoreManager();

            Arrays.stream(secondaryUSM).forEach(LambdaExceptionUtils.rethrowConsumer(secondaryDomain -> {
                org.wso2.carbon.user.core.UserStoreManager secUSM = userStoreManager
                        .getSecondaryUserStoreManager(secondaryDomain);
                validateUSM(secUSM, secondaryDomain, errors);
            }));
        } catch (UserStoreException e) {
            throw new BadHealthException(Constants.ErrorCodes.ERROR_LISTING_USERSTORES,
                    "Error while listing secondary user stores", errors, e);
        }

        if (!errors.isEmpty()) {
            throw new BadHealthException("Bad health found in super tenant user stores", errors);
        }
        return properties;
    }

    protected String[] getSecondaryUSMNames(RealmConfiguration secondaryRealmConfiguration) {

        List<String> userstores = new ArrayList<>();
        if (secondaryRealmConfiguration == null) {
            return new String[0];
        } else {

            do {
                Map<String, String> userStoreProperties = secondaryRealmConfiguration.getUserStoreProperties();
                Boolean disabled = false;
                if (userStoreProperties.get(DISABLED) != null) {
                    disabled = Boolean.valueOf(userStoreProperties.get(DISABLED));
                }
                if (!disabled) {
                    userstores.add(secondaryRealmConfiguration.getUserStoreProperty(UserStoreConfigConstants
                            .DOMAIN_NAME));
                }

                secondaryRealmConfiguration = secondaryRealmConfiguration.getSecondaryRealmConfig();

            } while (secondaryRealmConfiguration != null);
        }
        return userstores.toArray(new String[userstores.size()]);
    }

    protected void validateUSM(UserStoreManager userStoreManager, String domainName, List<HealthCheckError> errors) {

        if (!isUserStoreMonitored(domainName)) {
            return;
        }
        try {
            userStoreManager.isExistingRole(TEST_USER);
        } catch (Throwable e) {
            // Catching throwable since all kinds of errors must be captured including runtime ones.
            if (log.isDebugEnabled()) {
                log.debug("Error while checking existence of user in user store " + domainName, e);
            }
            errors.add(new HealthCheckError(Constants.ErrorCodes.ERROR_USERSTORE_CONNECTIVITY_BY_IS_USER_EXISTING
                    , "Error while checking health of USM with domain: " + domainName,
                    Utils.getRootCauseMessage(e)));
        }
    }

    protected void initMonitoredUserStores(HealthCheckerConfig healthCheckerConfig) {

        Object datasourcesObj = healthCheckerConfig.getProperties().get(MONITORED_USER_STORES);
        if (datasourcesObj != null) {
            String[] datasorucesArray = datasourcesObj.toString().split(",");
            monitoredUserStores = Arrays.asList(datasorucesArray);
            monitorAllUserStores = false;
        }
    }

    protected boolean isUserStoreMonitored(String name) {

        return (monitoredUserStores.stream().anyMatch(userstoreName -> userstoreName.equalsIgnoreCase(name)) ||
                monitorAllUserStores);
    }

}

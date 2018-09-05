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

package org.wso2.carbon.healthcheck.api.core.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.healthcheck.api.core.model.HealthCheckerConfig;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import static org.wso2.carbon.healthcheck.api.core.Constants.HealthCheckConfig.DEFAULT_NAMESPACE;
import static org.wso2.carbon.healthcheck.api.core.Constants.HealthCheckConfig.HEALTH_CHECKER;
import static org.wso2.carbon.healthcheck.api.core.Constants.HealthCheckConfig.HEALTH_CHECKERS;
import static org.wso2.carbon.healthcheck.api.core.Constants.HealthCheckConfig.NAME;
import static org.wso2.carbon.healthcheck.api.core.Constants.HealthCheckConfig.ORDER;
import static org.wso2.carbon.healthcheck.api.core.Constants.HealthCheckConfig.ENABLE;
import static org.wso2.carbon.healthcheck.api.core.Constants.HealthCheckConfig.PROPERTY;
import static org.wso2.carbon.healthcheck.api.core.Constants.HealthCheckConfig.PROPERTY_NAME;
import static org.wso2.carbon.healthcheck.api.core.Constants.HealthCheckConfig.HEALTH_CHECK_CONFIG_FILE_NAME;

/**
 * Parses configurations in health-check-config.xml and build configuration objects.
 */
public class HealthCheckConfigParser {

    private boolean isHealthCheckServiceEnabled = false;
    private final static Log log = LogFactory.getLog(HealthCheckConfigParser.class);
    private static HealthCheckConfigParser instance = new HealthCheckConfigParser();
    private Map<String, HealthCheckerConfig> healthCheckerConfigMap = new HashMap<>();

    public Map<String, HealthCheckerConfig> getHealthCheckerConfigMap() {

        return healthCheckerConfigMap;
    }

    public static HealthCheckConfigParser getInstance() {

        return instance;
    }

    private HealthCheckConfigParser() {

        init();
    }

    private void init() {

        try {
            buildConfiguration();
        } catch (FileNotFoundException e) {
            // Configuration file is not mandatory. Hence log info and continue.
            log.info("Configurations for carbon health service cannot be found");
            if (log.isDebugEnabled()) {
                log.error(e);
            }
        } catch (Throwable e) {
            // Catching throwable to avoid xml parse exceptions.
            log.error("Error while building configurations from: " + HEALTH_CHECK_CONFIG_FILE_NAME, e);
        }
    }

    private void buildConfiguration() throws FileNotFoundException, XMLStreamException {

        StAXOMBuilder builder;
        InputStream inStream = null;
        String configFilePath = CarbonUtils.getCarbonConfigDirPath();
        try {

            File heathCheckerXML = new File(configFilePath, HEALTH_CHECK_CONFIG_FILE_NAME);

            if (heathCheckerXML.exists()) {
                inStream = new FileInputStream(heathCheckerXML);
            }

            if (inStream == null) {
                String message = "HealthChecker Configuration not found at: " + configFilePath;
                if (log.isDebugEnabled()) {
                    log.debug(message);
                }
                throw new FileNotFoundException(message);
            }

            builder = new StAXOMBuilder(inStream);
            OMElement rootElement = builder.getDocumentElement();

            if (rootElement == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Returning from config builder since no root element found. Health Check Service is " +
                            "disabled");
                }
                return;
            }

            OMElement carbonHealthCheckConfigs = rootElement.getFirstChildWithName(
                    new QName(DEFAULT_NAMESPACE, "CarbonHealthCheckConfigs"));

            if (carbonHealthCheckConfigs == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Returning from config builder since no config element found for " +
                            "CarbonHealthCheckConfigs Health check service is disabled");
                }
                return;
            }
            evaluateHealthCheckEnabled(carbonHealthCheckConfigs);

            buildHealthCheckerConfigs(carbonHealthCheckConfigs.getFirstChildWithName(new QName(DEFAULT_NAMESPACE,
                    HEALTH_CHECKERS)));

        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (IOException e) {
                log.error("Error closing the input stream for " + HEALTH_CHECK_CONFIG_FILE_NAME, e);
            }
        }
    }

    private void evaluateHealthCheckEnabled(OMElement heathCheckConfigs) {

        OMElement enableConfig = heathCheckConfigs.getFirstChildWithName
                (new QName(DEFAULT_NAMESPACE, "Enable"));
        if (enableConfig == null) {
            if (log.isDebugEnabled()) {
                log.debug("No enable config found, Hence Health check service is disabled");
            }
            return;
        }
        isHealthCheckServiceEnabled = Boolean.parseBoolean(enableConfig.getText());
    }

    private void buildHealthCheckerConfigs(OMElement configs) {

        if (configs != null) {
            Iterator<OMElement> healthCheckers = configs.getChildrenWithName(
                    new QName(DEFAULT_NAMESPACE, HEALTH_CHECKER));

            if (healthCheckers != null) {
                while (healthCheckers.hasNext()) {
                    OMElement checkerElement = healthCheckers.next();
                    String healthCheckerName = checkerElement.getAttributeValue(new QName(NAME));
                    int order = 0;
                    try {
                        order = Integer.parseInt(checkerElement.getAttributeValue(new QName(ORDER)));
                    } catch (NumberFormatException e) {
                        log.error("Error while parsing given number. Hence order is 0");
                        if (log.isDebugEnabled()) {
                            log.debug(e);
                        }
                    }
                    boolean enable = Boolean.parseBoolean(checkerElement.getAttributeValue(new QName(ENABLE)));
                    Iterator<OMElement> propertyElements = checkerElement.getChildrenWithName(new QName(PROPERTY));
                    Properties properties = new Properties();
                    while (propertyElements.hasNext()) {
                        OMElement propertyElem = propertyElements.next();
                        String propertyName = propertyElem.getAttributeValue(new QName(PROPERTY_NAME));
                        String propertyValue = propertyElem.getText();
                        properties.setProperty(propertyName, propertyValue);
                    }
                    healthCheckerConfigMap.put(healthCheckerName, new HealthCheckerConfig(healthCheckerName, order,
                            enable, properties));

                }
            }

        }
    }

    public boolean isHealthCheckServiceEnabled() {

        return isHealthCheckServiceEnabled;
    }
}

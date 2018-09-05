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

import java.util.Comparator;

/*
 * Comparator for sorting HealthChecker collection
 */
public class HealthCheckerComparator implements Comparator<HealthChecker> {

    @Override
    public int compare(HealthChecker healthChecker1, HealthChecker healthChecker2) {

        if (healthChecker1.getOrder() > healthChecker2.getOrder()) {
            return 1;
        } else if (healthChecker1.getOrder() == healthChecker1.getOrder()) {
            return 0;
        } else {
            return -1;
        }
    }
}

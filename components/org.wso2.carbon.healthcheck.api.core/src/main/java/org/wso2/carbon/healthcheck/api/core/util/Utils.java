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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * Utilities for Carbon Health Check.
 */
public class Utils {

    /**
     * Returns the root cause for the throwable.
     *
     * @param throwable Throwable
     * @return The root cause for the throwable.
     */
    public static String getRootCauseMessage(Throwable throwable) {

        String rootCauseMessage = throwable.getMessage();
        Throwable rootCause = ExceptionUtils.getRootCause(throwable);
        if (rootCause != null && StringUtils.isNotEmpty(rootCause.getMessage())) {
            rootCauseMessage = rootCause.getMessage();
        }
        return rootCauseMessage;
    }
}

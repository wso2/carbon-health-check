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

package org.wso2.carbon.healthcheck.api.core.exception;

import java.util.Collections;
import java.util.List;

/**
 * Used to communicate errors from Health Check Service.
 */
public class HealthCheckServiceException extends Exception {

    private List<HealthCheckError> errors;

    public HealthCheckServiceException(String errorMessage, List<HealthCheckError> errors) {

        super(errorMessage);
        this.errors = errors;
    }

    public HealthCheckServiceException(String errorMessage, List<HealthCheckError> errors, Throwable e) {

        super(errorMessage, e);
        this.errors = errors;

    }

    public List<HealthCheckError> getErrors() {

        return errors == null ? Collections.emptyList() : Collections.unmodifiableList(errors);
    }
}

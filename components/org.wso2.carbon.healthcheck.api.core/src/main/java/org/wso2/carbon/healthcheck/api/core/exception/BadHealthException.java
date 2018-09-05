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
 * This exception is used to convey a failure in a HealthChecker.  There can be multiple errors which caused to the
 * failure.
 */
public class BadHealthException extends Exception {

    // List of errors which caused failure.
    private String errorCode;
    private List<HealthCheckError> errors;

    /**
     * When there is no cause for the exception.
     *
     * @param message Error message.
     * @param errors  List of errors which caused the health failure.
     */
    public BadHealthException(String message, List<HealthCheckError> errors) {

        super(message);
        this.errors = errors;
    }

    public BadHealthException(String errorCode, String errorMessage) {

        super(errorMessage);
        this.errorCode = errorCode;
    }

    public BadHealthException(String errorCode, String errorMessage, Throwable throwable) {

        super(errorMessage, throwable);
        this.errorCode = errorCode;
    }

    /**
     * @param errorMessage Error message.
     * @param errors       Errors which caused the health check failure.
     * @param e            Throwable.
     */
    public BadHealthException(String errorCode, String errorMessage, List<HealthCheckError> errors, Throwable e) {

        super(errorMessage, e);
        this.errorCode = errorCode;
        this.errors = errors;
    }

    /**
     * Returns list of errors which caused health check failure.
     *
     * @return List of errors which caused health check failure.
     */
    public List<HealthCheckError> getErrors() {

        return errors == null ? Collections.emptyList() : Collections.unmodifiableList(errors);
    }

    public String getErrorCode() {

        return errorCode;
    }
}

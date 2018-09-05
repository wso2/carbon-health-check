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

/**
 * An error which consist of Heath Check errors
 */
public class HealthCheckError {

    private String errorCode;
    private String message;
    private String errorDescription;

    /**
     * Constructor for Health Check Error.
     * @param errorCode Error code.
     * @param errorMessage Error message.
     * @param errorDescription Error description.
     */
    public HealthCheckError(String errorCode, String errorMessage, String errorDescription) {

        this.errorCode = errorCode;
        this.message = errorMessage;
        this.errorDescription = errorDescription;
    }

    public HealthCheckError(String errorCode, String errorMessage) {

        this.errorCode = errorCode;
        this.message = errorMessage;

    }

    public String getMessage() {

        return message;
    }

    public String getErrorCode() {

        return errorCode;
    }

    public String getErrorDescription() {

        return errorDescription;
    }
}

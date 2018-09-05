/*
 *
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.healthcheck.api.endpoint.expmapper;

import org.apache.cxf.jaxrs.impl.WebApplicationExceptionMapper;
import org.wso2.carbon.healthcheck.api.endpoint.exception.BadHealthException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Exception mapper used to avoid printing stack traces for Health Check failures.
 */
public class HealthCheckEndpointExceptionMapper extends WebApplicationExceptionMapper {

    public Response toResponse(WebApplicationException ex) {

        if (ex instanceof BadHealthException) {
            this.setPrintStackTrace(false);
        }

        return super.toResponse(ex);
    }
}

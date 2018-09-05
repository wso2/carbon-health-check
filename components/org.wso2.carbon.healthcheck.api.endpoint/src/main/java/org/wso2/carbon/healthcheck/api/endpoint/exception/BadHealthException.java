package org.wso2.carbon.healthcheck.api.endpoint.exception;

import org.wso2.carbon.healthcheck.api.endpoint.dto.ErrorsDTO;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class BadHealthException extends WebApplicationException {

    public static final String DEFAULT_RESPONSE_CONTENT_TYPE = "application/json";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    public BadHealthException(ErrorsDTO errorsDTO) {

        super(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(errorsDTO)
                .header(HEADER_CONTENT_TYPE, DEFAULT_RESPONSE_CONTENT_TYPE)
                .build());
    }

    public BadHealthException() {

        super(Response.Status.SERVICE_UNAVAILABLE);
    }

    @Override
    public String getMessage() {

        return super.getMessage();
    }

}

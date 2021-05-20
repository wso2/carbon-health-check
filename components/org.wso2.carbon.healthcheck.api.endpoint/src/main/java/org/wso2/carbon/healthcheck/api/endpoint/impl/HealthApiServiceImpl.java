package org.wso2.carbon.healthcheck.api.endpoint.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.healthcheck.api.endpoint.exception.BadHealthException;
import org.wso2.carbon.healthcheck.api.core.CarbonHealthCheckService;
import org.wso2.carbon.healthcheck.api.core.exception.HealthCheckError;
import org.wso2.carbon.healthcheck.api.core.exception.HealthCheckServiceException;
import org.wso2.carbon.healthcheck.api.endpoint.ApiResponseMessage;
import org.wso2.carbon.healthcheck.api.endpoint.HealthApiService;
import org.wso2.carbon.healthcheck.api.endpoint.dto.ErrorDTO;
import org.wso2.carbon.healthcheck.api.endpoint.dto.ErrorsDTO;
import org.wso2.carbon.healthcheck.api.endpoint.dto.HealthCheckResponseDTO;
import org.wso2.carbon.healthcheck.api.endpoint.dto.PropertyDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.core.Response;

public class HealthApiServiceImpl extends HealthApiService {

    private static final Log log = LogFactory.getLog(HealthApiServiceImpl.class);

    @Override
    public Response healthGet() {

        CarbonHealthCheckService carbonHealthCheckService = getCarbonHealthCheckService();
        if (carbonHealthCheckService == null) {
            log.info("Carbon Health Check Service is not found from endpoint. Hence returning");
            return Response.ok().build();
        }
        try {
            HealthCheckResponseDTO responseDTO = getHealthCheckResponseDTO(carbonHealthCheckService);
            return Response.ok().entity(responseDTO).build();
        } catch (HealthCheckServiceException e) {
            return getErrorResponse(e);
        }
    }

    private Response getErrorResponse(HealthCheckServiceException e) {

        List<HealthCheckError> errors = e.getErrors();
        ErrorsDTO errorsResponseDTO = new ErrorsDTO();
        errors.forEach(error -> {
            log.error(error.getMessage());
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage(error.getMessage());
            errorDTO.setDescription(error.getErrorDescription());
            errorDTO.setCode(error.getErrorCode());
            errorsResponseDTO.getErrors().add(errorDTO);
        });

        BadHealthException badHealthException = new BadHealthException(errorsResponseDTO);
        if (log.isDebugEnabled()) {
            log.error("Health Check Failed", e);
        }
        throw badHealthException;
    }

    private HealthCheckResponseDTO getHealthCheckResponseDTO(CarbonHealthCheckService
                                                                     carbonHealthCheckService)
            throws HealthCheckServiceException {

        List<PropertyDTO> responseProperties = new ArrayList<>();
        Properties properties = carbonHealthCheckService.healthCheck();
        HealthCheckResponseDTO responseDTO = new HealthCheckResponseDTO();
        properties.forEach((key, value) -> {
            PropertyDTO propertyDTO = new PropertyDTO();
            propertyDTO.setKey(key.toString());
            propertyDTO.setValue(value.toString());
            responseProperties.add(propertyDTO);
        });
        responseDTO.setHealth(responseProperties);
        return responseDTO;
    }

    private static CarbonHealthCheckService getCarbonHealthCheckService() {

        return (CarbonHealthCheckService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(CarbonHealthCheckService.class, null);
    }
}

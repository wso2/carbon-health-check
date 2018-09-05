package org.wso2.carbon.healthcheck.api.endpoint;

import org.wso2.carbon.healthcheck.api.endpoint.dto.*;
import org.wso2.carbon.healthcheck.api.endpoint.HealthApiService;
import org.wso2.carbon.healthcheck.api.endpoint.factories.HealthApiServiceFactory;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.healthcheck.api.endpoint.dto.ErrorsDTO;
import org.wso2.carbon.healthcheck.api.endpoint.dto.HealthCheckResponseDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/health")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(value = "/health", description = "the health API")
public class HealthApi  {

   private final HealthApiService delegate = HealthApiServiceFactory.getHealthApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "List of key value pairs which contains responses from each heath checker.\n", notes = "This API check the health of a carbon product.\n", response = HealthCheckResponseDTO.class)
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
        
        @io.swagger.annotations.ApiResponse(code = 503, message = "Service Not Avaialable") })

    public Response healthGet()
    {
    return delegate.healthGet();
    }
}


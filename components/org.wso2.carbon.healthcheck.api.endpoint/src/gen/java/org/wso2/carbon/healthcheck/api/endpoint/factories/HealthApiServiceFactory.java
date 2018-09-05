package org.wso2.carbon.healthcheck.api.endpoint.factories;

import org.wso2.carbon.healthcheck.api.endpoint.HealthApiService;
import org.wso2.carbon.healthcheck.api.endpoint.impl.HealthApiServiceImpl;

public class HealthApiServiceFactory {

   private final static HealthApiService service = new HealthApiServiceImpl();

   public static HealthApiService getHealthApi()
   {
      return service;
   }
}

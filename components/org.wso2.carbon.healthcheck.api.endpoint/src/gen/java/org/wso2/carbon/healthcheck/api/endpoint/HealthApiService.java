package org.wso2.carbon.healthcheck.api.endpoint;

import javax.ws.rs.core.Response;

public abstract class HealthApiService {

    public abstract Response healthGet();
}


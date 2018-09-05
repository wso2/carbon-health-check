# carbon-health-check-api

This API is used to check the health of a carbon server.

# How to deploy

Carbon health check components can be deployed in two ways.

1) Install the carbon.healthcheck.server feature to your carbon product

2) You can copy org.wso2.carbon.healthcheck.api.core bundle to your dropins directory and copy the webapp api#health-check#v1.0.war to your repository/deployment/server/webapps. The configuration file "health-check.config.xml" is an optional file which you can copy to your repository/conf/ directory. 

Below is a sample configuration

```xml
<CarbonHealthCheckConfigs>

        <Enable>true</Enable>

        <HealthCheckers>
            <HealthChecker name="DataSourceHealthChecker" orderId="97" enable="true">
                <!--<Property name="monitored.datasources">jdbc/WSO2CarbonDB,jdbc/WSO2MetricsDB,jdbc/WSO2UMDB</Property>-->
                <Property name="pool.usage.limit.percentage">80</Property>
            </HealthChecker>
            <HealthChecker name="SuperTenantUSHealthChecker" orderId="98" enable="true">
                <!--<Property name="monitored.user.stores">primary,sec</Property>-->
            </HealthChecker>
        </HealthCheckers>

</CarbonHealthCheckConfigs> 

```

A health checker can be enabled or disabled using "enable" attribute. Furthermore the execution order can be decided using the orderId. The properties which are configured under each health checker will be available for each health checker at runtime.


Invoke API 

This is an open API which is ideally should be blocked from load balancer level. The request is a GET to the health check API. Below is the curl

curl -k -v https://{hostname}:{port}/api/health-check/v1.0/health

A success scenario is a 200 OK response with a list of health check results.

```json

{  
   "health":[  
      {  
         "key":"jdbc/WSO2CarbonDB.active.connection.count",
         "value":"1"
      },
      {  
         "key":"bpsds.active.connection.count",
         "value":"1"
      },
      {  
         "key":"jdbc/WSO2MetricsDB.connectivityTime.ms",
         "value":"81"
      },
      {  
         "key":"jdbc/WSO2MetricsDB.active.connection.count",
         "value":"1"
      },
      {  
         "key":"jdbc/WSO2CarbonDB.connectivityTime.ms",
         "value":"0"
      },
      {  
         "key":"bpsds.connectivityTime.ms",
         "value":"0"
      }
   ]
}
```

Sample Error Response : A 503 Service Unavailable response with an array of errors. 

```json
{  
   "errors":[  
      {  
         "code":"HC_00001",
         "message":"Error while getting database connection for datasource: jdbc/DISCONNECTED",
         "description":"Network is unreachable (connect failed)"
      },
      {  
         "code":"HC_00003",
         "message":"Error while checking health of USM with domain: SEC",
         "description":"Access denied for user 'roott'@'localhost' (using password: YES)"
      }
   ]
}
```

Error codes

1)  "HC_00001" - Data source connectivity error.
2)  "HC_00002" - Number of connections in data source exeeds the healthy percentage.
3)  "HC_00003" - Error while testing connectivity to userstore by operation isExistingUser
4)  "HC_00004" - Server status is not running
5)  "HC_00005" - Error listing user stores.
           
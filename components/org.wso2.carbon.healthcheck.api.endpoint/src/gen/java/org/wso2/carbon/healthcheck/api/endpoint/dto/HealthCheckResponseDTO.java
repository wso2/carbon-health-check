package org.wso2.carbon.healthcheck.api.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.healthcheck.api.endpoint.dto.PropertyDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class HealthCheckResponseDTO  {
  
  
  
  private List<PropertyDTO> health = new ArrayList<PropertyDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("health")
  public List<PropertyDTO> getHealth() {
    return health;
  }
  public void setHealth(List<PropertyDTO> health) {
    this.health = health;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class HealthCheckResponseDTO {\n");
    
    sb.append("  health: ").append(health).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

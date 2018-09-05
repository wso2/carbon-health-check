package org.wso2.carbon.healthcheck.api.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.healthcheck.api.endpoint.dto.ErrorDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ErrorsDTO  {
  
  
  
  private List<ErrorDTO> errors = new ArrayList<ErrorDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("errors")
  public List<ErrorDTO> getErrors() {
    return errors;
  }
  public void setErrors(List<ErrorDTO> errors) {
    this.errors = errors;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorsDTO {\n");
    
    sb.append("  errors: ").append(errors).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

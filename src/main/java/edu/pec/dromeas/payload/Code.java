package edu.pec.dromeas.payload;

import javax.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Code {
  // TODO see will large string cause a problem
  @NotBlank private String code;

  public String getCode() {
    return code;
  }
}

package edu.pec.dromeas.payload;

import java.util.Set;
import lombok.Data;

@Data
public class AllResults {
  private String language;
  private Set<Tests> tests;
}

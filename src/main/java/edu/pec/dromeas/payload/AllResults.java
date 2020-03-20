package edu.pec.dromeas.payload;

import lombok.Data;

import java.util.Set;

@Data
public class AllResults
{
    private String language;
    private Set<Tests> tests;
}

package edu.pec.dromeas.payload;

import java.util.Set;

public record AllResults(String language, Set<Tests> tests) {}

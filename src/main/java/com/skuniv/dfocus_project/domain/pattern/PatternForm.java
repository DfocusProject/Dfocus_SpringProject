package com.skuniv.dfocus_project.domain.pattern;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class PatternForm {
    private String patternName;
    private Map<String, String> dateCodeMap;
}

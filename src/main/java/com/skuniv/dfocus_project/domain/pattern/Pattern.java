package com.skuniv.dfocus_project.domain.pattern;

import com.skuniv.dfocus_project.dto.DateShiftDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Pattern {
    private String patternName;
    private Map<String, String> dateCodeMap; // 저장용
    private List<DateShiftDto> dateShiftList; // 조회용
}

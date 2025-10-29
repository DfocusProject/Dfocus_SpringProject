package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.domain.pattern.ShiftType;

import java.time.LocalDate;
import java.util.List;

public interface PatternMapper {
    List<ShiftType> selectAllShiftTypes();

    void insertPattern(String patternName);

    void insertPatternDetail(String patternName, LocalDate date, String code);
}

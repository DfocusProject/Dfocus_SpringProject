package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.domain.pattern.Pattern;
import com.skuniv.dfocus_project.domain.pattern.ShiftType;

import java.time.LocalDate;
import java.util.List;

public interface PatternMapper {
    List<ShiftType> selectAllShiftTypes();

    void insertPattern(String patternName);

    void insertPatternDetail(String patternName, LocalDate date, String code);

    // 모든 패턴 이름 조회
    List<String> selectAllPatternNames();

    // 패턴 리스트 조회 (단일 String 파라미터)
    List<Pattern> selectPatternList(String patternName);

    List<String> selectDeptsByPattern(String patternName);
}

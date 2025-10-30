package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.domain.pattern.Pattern;
import com.skuniv.dfocus_project.domain.pattern.ShiftType;
import com.skuniv.dfocus_project.dto.DateShiftDto;
import com.skuniv.dfocus_project.mapper.PatternMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class PatternService {

    private final PatternMapper patternMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional
    public void savePattern(Pattern form) {
        // 1. 패턴 저장
        patternMapper.insertPattern(form.getPatternName());
        form.getDateCodeMap().forEach((dateStr, code) -> {
            LocalDate date = LocalDate.parse(dateStr);
            patternMapper.insertPatternDetail(form.getPatternName(), date, code);
        });

        List<String> deptCodes = patternMapper.selectDeptsByPattern(form.getPatternName());

//        // 3. 부서 직원 조회
//        List<String> empCodes = patternMapper.selectEmpCodesByDeptCodes(deptCodes);
//
//        // 4. WEBEMPPLAN에 일정 추가
//        form.getDateCodeMap().forEach((dateStr, shiftCode) -> {
//            LocalDate workDate = LocalDate.parse(dateStr);
//            for (String empCode : empCodes) {
//                String holidayYn = "Y";
//                if ("13".equals(shiftCode)) holidayYn = "N";
//                patternMapper.insertEmpPlan(workDate, empCode, shiftCode, form.getPatternName(), holidayYn);
//            }
//        });
    }


    // 모든 근태코드 조회
    public List<ShiftType> getShiftTypes() {
        return patternMapper.selectAllShiftTypes();
    }

    // 모든 패턴 이름 조회
    public List<String> getPatternNames() {
        return patternMapper.selectAllPatternNames();
    }

    // 단일 패턴 조회
    public Pattern getPattern(String patternName) {
        Pattern pattern = patternMapper.selectPatternList(patternName).stream().findFirst().orElse(null);

        if (pattern != null && pattern.getDateShiftList() != null) {
            Map<String, String> dateCodeMap = new HashMap<>();
            for (DateShiftDto ds : pattern.getDateShiftList()) {
                // LocalDate → 문자열 변환
                dateCodeMap.put(ds.getDate(), ds.getShiftCode());
            }
            pattern.setDateCodeMap(dateCodeMap);
        }

        return pattern;
    }

    public List<Pattern> getAllPatterns() {
        List<String> patternNames = patternMapper.selectAllPatternNames();
        List<Pattern> patterns = new ArrayList<>();
        for (String name : patternNames) {
            Pattern pattern = getPattern(name);
            if (pattern != null) patterns.add(pattern);
        }
        return patterns;
    }

}

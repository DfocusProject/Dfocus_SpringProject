package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.domain.pattern.Pattern;
import com.skuniv.dfocus_project.domain.pattern.ShiftType;
import com.skuniv.dfocus_project.dto.DateShiftDto;
import com.skuniv.dfocus_project.dto.DeptDto;
import com.skuniv.dfocus_project.mapper.DeptMapper;
import com.skuniv.dfocus_project.mapper.EmpMapper;
import com.skuniv.dfocus_project.mapper.PatternMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class PatternService {

    private final PatternMapper patternMapper;
    private final EmpMapper empMapper;
    private final DeptMapper deptMapper;

    @Transactional
    public void savePattern(Pattern form) {
        form.getDateCodeMap().forEach((dateStr, code) -> {
            LocalDate date = LocalDate.parse(dateStr);
            patternMapper.upsertPatternDetail(form.getPatternName(), date, code);
        });

        List<String> empCodes = empMapper.selectEmpsByPattern(form.getPatternName());

        form.getDateCodeMap().forEach((dateStr, shiftCode) -> {
            LocalDate workDate = LocalDate.parse(dateStr);
            for (String empCode : empCodes) {
                String holidayYn = "N";
                if ("13".equals(shiftCode) || "12".equals(shiftCode)) {
                    holidayYn = "Y";
                }
                DeptDto dept = deptMapper.getDeptByEmpCode(empCode);
                boolean exists = empMapper.existsEmpPlan(workDate, empCode);
                if (exists) {
                    empMapper.updateEmpPlan(workDate, empCode, dept.getDeptCode(), shiftCode, form.getPatternName(), holidayYn);
                } else {
                    empMapper.insertEmpPlan(workDate, empCode, dept.getDeptCode(), shiftCode, form.getPatternName(), holidayYn);
                }            }
        });
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

    public void createPattern(String patternName, String description) {
        patternMapper.createPattern(patternName, description);
    }
}

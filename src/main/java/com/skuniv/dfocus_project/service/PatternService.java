package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.domain.pattern.PatternForm;
import com.skuniv.dfocus_project.domain.pattern.ShiftType;
import com.skuniv.dfocus_project.mapper.PatternMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class PatternService {
    private final PatternMapper patternMapper;
    public void savePattern(PatternForm form) {
        patternMapper.insertPattern(form.getPatternName());

        form.getDateCodeMap().forEach((dateStr, code) -> {
            LocalDate date = LocalDate.parse(dateStr);
            patternMapper.insertPatternDetail(form.getPatternName(), date, code);
        });
    }

    public List<ShiftType> getShiftTypes() {

        return patternMapper.selectAllShiftTypes();
    }
}

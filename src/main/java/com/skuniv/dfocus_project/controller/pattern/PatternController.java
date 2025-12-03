package com.skuniv.dfocus_project.controller.pattern;

import com.skuniv.dfocus_project.domain.pattern.Pattern;
import com.skuniv.dfocus_project.domain.pattern.ShiftType;
import com.skuniv.dfocus_project.service.PatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pattern")
public class PatternController {

    private final PatternService patternService;

    @GetMapping("/main")
    public String workPattern(Model model,
                              @RequestParam(required = false) YearMonth yearMonth) {
        if (yearMonth == null) {
            yearMonth = YearMonth.now();
        }
        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();

        LocalDate start = LocalDate.of(year, month, 1);
        int lengthOfMonth = start.lengthOfMonth();

        List<LocalDate> dates = new ArrayList<>();
        for (int i = 1; i <= lengthOfMonth; i++) {
            dates.add(start.withDayOfMonth(i));
        }

        List<ShiftType> shiftTypes = patternService.getShiftTypes();

        // ★ 여기서 패턴 조회 필요
        List<Pattern> patternList = patternService.getAllPatterns(); // Service에 메서드 추가
        model.addAttribute("patternList", patternList);

        model.addAttribute("shiftTypes", shiftTypes);
        model.addAttribute("dates", dates);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("selectedYearMonth", yearMonth);

        return "pattern/main";
    }


    @PostMapping("/create")
    public String createPattern(@RequestParam String patternName,
                                @RequestParam String description
                                ) {
        patternService.createPattern(patternName, description);
        return "redirect:/pattern/main";
    }

    @PostMapping("/save")
    public String savePatterns(@ModelAttribute Pattern form) {
        patternService.savePattern(form);
        return "redirect:/pattern/main";
    }

    @PostMapping("/delete")
    public String deletePattern(@RequestParam String patternCode) {
        patternService.deletePattern(patternCode);
        return "redirect:/pattern/main";
    }
}

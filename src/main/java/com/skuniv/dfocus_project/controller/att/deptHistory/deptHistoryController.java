package com.skuniv.dfocus_project.controller.att.deptHistory;

import com.skuniv.dfocus_project.CustomUserDetails;
import com.skuniv.dfocus_project.dto.att.DeptAttSearchDto;
import com.skuniv.dfocus_project.dto.att.DeptAttSearchResultDto;
import com.skuniv.dfocus_project.service.DeptHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/deptAttSearch")
@SessionAttributes("deptAttSearchDto")  // 검색조건 DTO를 세션에서 유지
public class deptHistoryController {
    private final DeptHistoryService deptHistoryService;
    @ModelAttribute("deptAttSearchDto")
    public DeptAttSearchDto createSearchDto(Authentication authentication) {
        CustomUserDetails loginUser = (CustomUserDetails) authentication.getPrincipal();

        DeptAttSearchDto dto = new DeptAttSearchDto();
        dto.setWorkDate(LocalDate.now());
        dto.setDepartment(loginUser.getDeptCode());
        return dto;
    }

    @GetMapping("/main")
    public String main(Authentication authentication, Model model) {
        CustomUserDetails loginUser = (CustomUserDetails) authentication.getPrincipal();
        String deptCode = loginUser.getDeptCode();
        List<String> childDeptList = deptHistoryService.getChildDeptList(deptCode);
        model.addAttribute("childeDeptList", childDeptList);
        return "deptAttSearch/main";
    }
    @GetMapping("/search")
    public String search(Model model, @ModelAttribute("deptAttSearchDto") DeptAttSearchDto deptAttSearchDto) {
        model.addAttribute("startDate", deptAttSearchDto.getWorkDate().with(DayOfWeek.MONDAY));
        model.addAttribute("endDate", deptAttSearchDto.getWorkDate().with(DayOfWeek.SUNDAY));
        List<DeptAttSearchResultDto> results = deptHistoryService.getEmpList(deptAttSearchDto);
        deptHistoryService.calculateDeptAttResult(results, deptAttSearchDto.getWorkDate());
        model.addAttribute("results", results);
        return "deptAttSearch/main";
    }
}

package com.skuniv.dfocus_project.controller.att.request;

import com.skuniv.dfocus_project.CustomUserDetails;
import com.skuniv.dfocus_project.dto.*;
import com.skuniv.dfocus_project.service.AttService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/att/general")
public class AttController {

    private final AttService attService;

    @GetMapping("/main")
    public String mainPage() {
        return "att/general";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String attType,
            @RequestParam(required = false) LocalDate workDate,
            @RequestParam(required = false) List<String> empCode,
            Model model,
            Authentication authentication
    ) {

        if (workDate == null) workDate = LocalDate.now();

        CustomUserDetails loginUser = (CustomUserDetails) authentication.getPrincipal();
        String loginRole = loginUser.getRole();
        String deptName = loginUser.getDeptName();

        List<String> searchEmpCode;
        if ("LEADER".equals(loginRole)) {
            searchEmpCode = empCode;
        } else {
            searchEmpCode = Collections.singletonList(loginUser.getUsername());
        }

        // 근태 대상자 조회 + 휴일 로직 처리
        List<AttEmpViewDto> empList = attService.getAttEmpListWithHolidayCheck(attType, workDate, searchEmpCode, deptName);
        model.addAttribute("attType", attType);
        model.addAttribute("workDate", workDate);
        model.addAttribute("empList", empList);

        return "att/general";
    }


    @PostMapping("/{type}") // type = "on" 또는 "off"
    public String recordCommute(
            @PathVariable String type,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String empCode = userDetails.getUsername(); // 로그인 사용자 empCode
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();

        if ("on".equals(type)) {
            attService.recordOnCommute(empCode, today, now);
        } else if ("off".equals(type)) {
            attService.recordOffCommute(empCode, today, now);
        } else {
            throw new IllegalArgumentException("Invalid commute type: " + type);
        }

        return "redirect:/home";
    }


    @PostMapping("/save")
    public String saveAttendance(@ModelAttribute AttendanceRequestDto request,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 세션 대신 @AuthenticationPrincipal로 로그인 사용자 정보 가져오기
        String empCode = userDetails.getUsername(); // getUsername() -> empCode

        String message = attService.saveAttendance(request.getWorkDate(), request.getAttList(), empCode);

        if (message != null) {
            redirectAttributes.addFlashAttribute("error", message);
        }
        redirectAttributes.addAttribute("attType", request.getAttList().getFirst().getAttType());
        redirectAttributes.addAttribute("workDate", request.getWorkDate());
        return "redirect:/att/general/search";
    }


    @PostMapping("/request")
    public String submitAttendance(
            @ModelAttribute AttendanceRequestDto request,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 세션 대신 로그인 사용자 정보에서 empCode 가져오기
        String empCode = userDetails.getUsername(); // getUsername()이 empCode 반환

        String error = attService.requestAttendance(request.getWorkDate(), request.getAttList(), empCode);

        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            redirectAttributes.addAttribute("attType", request.getAttList().getFirst().getAttType());
            redirectAttributes.addAttribute("workDate", request.getWorkDate());
            return "redirect:/att/general/search"; // 오류 있으면 다시 돌아가기
        }

        redirectAttributes.addFlashAttribute("message", "상신이 완료되었습니다.");

        redirectAttributes.addAttribute("attType", request.getAttList().getFirst().getAttType());
        redirectAttributes.addAttribute("workDate", request.getWorkDate());
        return "redirect:/att/general/main";
    }

    @PostMapping("/delete")
    public String delete(@ModelAttribute AttendanceRequestDto request, RedirectAttributes redirectAttributes){
        attService.deleteAttendance(request.getAttList());
        redirectAttributes.addAttribute("attType", request.getAttList().getFirst().getAttType());
        redirectAttributes.addAttribute("workDate", request.getWorkDate());
        return "redirect:/att/general/main";
    }
    @PostMapping("/requestCancel")
    public String requestCancel(@ModelAttribute AttendanceRequestDto request, RedirectAttributes redirectAttributes){
        attService.cancelAttendance(request.getAttList());
        redirectAttributes.addAttribute("attType", request.getAttList().getFirst().getAttType());
        redirectAttributes.addAttribute("workDate", request.getWorkDate());
        return "redirect:/att/general/main";
    }
}

package com.skuniv.dfocus_project.controller;

import com.skuniv.dfocus_project.CustomUserDetails;
import com.skuniv.dfocus_project.domain.Time.TimeRange;
import com.skuniv.dfocus_project.domain.account.Account;
import com.skuniv.dfocus_project.dto.*;
import com.skuniv.dfocus_project.service.AttService;
import com.skuniv.dfocus_project.service.DeptService;
import com.skuniv.dfocus_project.service.EmpService;
import com.skuniv.dfocus_project.service.PatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/att")
public class AttController {

    private final DeptService deptService;
    private final EmpService empService;
    private final AttService attService;
    private final PatternService patternService;

    @GetMapping("/general")
    public String getUserInfo() {
        return "att/general";
    }

    @GetMapping("/etc")
    public String etc(){
        return "att/etc";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String attType,
            @RequestParam(required = false) LocalDate workDate,
            @RequestParam(required = false) String empCode,
            Model model,
            Authentication authentication
    ) {

        if (attType == null) attType = "연장";
        if (workDate == null) workDate = LocalDate.now();

        // 로그인 사용자 정보
        CustomUserDetails loginUser = (CustomUserDetails) authentication.getPrincipal();
        String loginRole = loginUser.getRole();   // ROLE_USER, ROLE_ADMIN 등
        String deptName = loginUser.getDeptName();

        String searchEmpCode;
        if ("LEADER".equals(loginRole)) {
            searchEmpCode = empCode != null ? empCode : ""; // LEADER는 선택적으로 사번 조회 가능
        } else {
            searchEmpCode = loginUser.getUsername(); // USER는 자기 사번만
        }

        // 근태 대상자 조회
        List<AttEmpViewDto> empList = empService.getAttEmpList(attType, workDate, searchEmpCode, deptName);

        // 추가 데이터 처리
        for (AttEmpViewDto emp : empList) {

            String realWorkRecord = attService.getRealWorkRecord(emp.getEmpCode(), workDate);
            emp.setRealWorkRecord(realWorkRecord);

            double expectedWorkHours = attService.getWeeklyWorkHours(emp.getEmpCode(), workDate);
            emp.setExpectedWorkHours(expectedWorkHours);

            if (emp.getRequestId() == null) {
                TimeRange planTime = attService.getPlannedCommuteTime(emp.getEmpCode(), workDate);
                emp.setPlannedStartTime(planTime.getStartTime());
                emp.setPlannedEndTime(planTime.getEndTime());
            }
        }

        // 검색 유지용만 전달
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
        redirectAttributes.addAttribute("attType", request.getAttList().get(0).getAttType());
        redirectAttributes.addAttribute("workDate", request.getWorkDate());
        return "redirect:/att/search";
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
            return "redirect:/att/general"; // 오류 있으면 다시 돌아가기
        }

        redirectAttributes.addFlashAttribute("message", "상신이 완료되었습니다.");

        redirectAttributes.addAttribute("attType", request.getAttList().get(0).getAttType());
        redirectAttributes.addAttribute("workDate", request.getWorkDate());
        return "redirect:/att/general";
    }

    @PostMapping("/delete")
    public String delete(@ModelAttribute AttendanceRequestDto request){
        attService.deleteAttendance(request.getAttList());
        return "redirect:/att/general";
    }
    @PostMapping("/requestCancel")
    public String requestCancel(@ModelAttribute AttendanceRequestDto request){
        attService.cancelAttendance(request.getAttList());
        return "redirect:/att/general";
    }
}

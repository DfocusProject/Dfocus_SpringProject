package com.skuniv.dfocus_project.controller;

import com.skuniv.dfocus_project.domain.Time.TimeRange;
import com.skuniv.dfocus_project.domain.account.Account;
import com.skuniv.dfocus_project.dto.*;
import com.skuniv.dfocus_project.service.AttService;
import com.skuniv.dfocus_project.service.DeptService;
import com.skuniv.dfocus_project.service.EmpService;
import com.skuniv.dfocus_project.service.PatternService;
import lombok.RequiredArgsConstructor;
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
    public String getUserInfo(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // 로그인 계정 확인
        Account loginAccount = (Account) session.getAttribute("loginAccount");
        if (loginAccount == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/";
        }

        // 부서 정보 조회
        DeptDto deptDto = deptService.getDeptByEmpCode(loginAccount.getEmpCode());
        if (deptDto == null) {
            redirectAttributes.addFlashAttribute("error", "부서 정보가 없어 근태 신청이 불가합니다.");
            return "redirect:/home";
        }

        // 모델에 기본 정보 담기 (검색 유지 및 뷰용)
        model.addAttribute("empCode", loginAccount.getEmpCode());
        model.addAttribute("deptName", deptDto.getDeptName());
        model.addAttribute("role", loginAccount.getRole().name());

        return "att/general";
    }

    @GetMapping("/etc")
    public String etc(){
        return "att/etc";
    }

    @GetMapping("/search")
    public String search(@RequestParam String attType,
                         @RequestParam LocalDate workDate,
                         @RequestParam(required = false) String empCode,
                         Model model,
                         HttpSession session) {

        Account loginAccount = (Account) session.getAttribute("loginAccount");
        String searchEmpCode = "USER".equals(loginAccount.getRole().name()) ? loginAccount.getEmpCode() : empCode;
        DeptDto deptDto = deptService.getDeptByEmpCode(loginAccount.getEmpCode());
        String deptName = deptDto.getDeptName();
        // 근태 대상자 리스트 조회 및 데이터 가공
        List<AttEmpViewDto> empList = empService.getAttEmpList(attType, workDate, searchEmpCode, deptName);

        for (AttEmpViewDto emp : empList) {
            // 실근무 기록 계산
            String realWorkRecord = attService.getRealWorkRecord(emp.getEmpCode(), workDate);
            emp.setRealWorkRecord(realWorkRecord);

            double expectedWorkHours = attService.getWeeklyWorkHours(emp.getEmpCode(), workDate);
            emp.setExpectedWorkHours(expectedWorkHours);

            // 계획 근무시간 조회
            if(emp.getRequestId() == null) {
                TimeRange planTime = attService.getPlannedCommuteTime(emp.getEmpCode(), workDate);
                emp.setPlannedStartTime(planTime.getStartTime());
                emp.setPlannedEndTime(planTime.getEndTime());
            }
            // 저장/상신된 근태 정보는 이미 LEFT JOIN으로 empList에 포함되어 있음
            // 따라서 savedAttendance 조회는 필요 없음
        }

        // 검색 유지용 모델
        model.addAttribute("empCode", searchEmpCode);
        model.addAttribute("attType", attType);
        model.addAttribute("workDate", workDate);
        model.addAttribute("deptName", deptName);
        model.addAttribute("role", loginAccount.getRole().name());
        model.addAttribute("empList", empList);

        return "att/general";
    }


    @PostMapping("/{type}") // type = "on" 또는 "off"
    public String recordCommute(
            @PathVariable String type,
            HttpSession session
    ) {

        Account loginAccount = (Account) session.getAttribute("loginAccount");
        String empCode = loginAccount.getEmpCode();
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
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Account loginAccount = (Account) session.getAttribute("loginAccount");
        String message = attService.saveAttendance(request.getWorkDate(), request.getAttList(), loginAccount.getEmpCode());

        if (message != null) {
            redirectAttributes.addFlashAttribute("error", message);
        }
        return "redirect:/att/general";
    }

    @PostMapping("/request")
    public String submitAttendance(@ModelAttribute AttendanceRequestDto request,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Account loginAccount = (Account) session.getAttribute("loginAccount");
        String error = attService.requestAttendance(request.getWorkDate(), request.getAttList(), loginAccount.getEmpCode());

        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/att/general"; // 오류 있으면 다시 돌아가기
        }

        redirectAttributes.addFlashAttribute("message", "상신이 완료되었습니다.");
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

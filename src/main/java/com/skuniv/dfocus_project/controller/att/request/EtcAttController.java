package com.skuniv.dfocus_project.controller.att.request;

import com.skuniv.dfocus_project.CustomUserDetails;
import com.skuniv.dfocus_project.dto.AttEmpViewDto;
import com.skuniv.dfocus_project.dto.EtcRequestDto;
import com.skuniv.dfocus_project.service.AttService;
import com.skuniv.dfocus_project.service.EmpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/att/etc")
public class EtcAttController {
    private final EmpService empService;
    private final AttService attService;

    @GetMapping("/main")
    public String mainPage(Model model){
        model.addAttribute("workDate", LocalDate.now());
        return "att/etc";
    }

    @GetMapping("/search")
    public String searchEtcAtt(
            @RequestParam(required = false) LocalDate workDate,
            @RequestParam(required = false) String empCode,
            @RequestParam(required = false) String planType,
            Model model,
            Authentication authentication
    ){
        if (workDate == null) workDate = LocalDate.now();

        // 로그인 사용자 정보
        CustomUserDetails loginUser = (CustomUserDetails) authentication.getPrincipal();
        String deptName = loginUser.getDeptName();

        String searchEmpCode;
        if ("LEADER".equals(loginUser.getRole())) {
            searchEmpCode = empCode != null ? empCode : ""; // LEADER는 선택적으로 사번 조회 가능
        } else {
            searchEmpCode = loginUser.getUsername(); // USER는 자기 사번만
        }
        List<AttEmpViewDto> empList = empService.getEtcAttEmpList(planType, workDate, searchEmpCode, deptName);
        for(AttEmpViewDto emp : empList){
            emp.setRealWorkRecord(attService.getRealWorkRecord(emp.getEmpCode(), workDate));
        }
        model.addAttribute("workDate", workDate);
        model.addAttribute("planType", planType);
        model.addAttribute("empList", empList);
        return "att/etc";
    }
    @PostMapping("/save")
    public String saveAttendance(@ModelAttribute EtcRequestDto request,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {

        String applicant = userDetails.getUsername();

        String message = attService.saveEtcAttendance(request.getWorkDate(), request.getEtcList(), applicant);
        System.out.println("message = " + message);
        if (message != null) {
            redirectAttributes.addFlashAttribute("error", message);
        }
        redirectAttributes.addAttribute("planType", request.getEtcList().get(0).getPlanType());
        redirectAttributes.addAttribute("workDate", request.getWorkDate());
        return "redirect:/att/etc/search";
    }
    @PostMapping("/request")
    public String requestAttendance(
            @ModelAttribute EtcRequestDto request,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String empCode = userDetails.getUsername();

        String message = attService.requestEtcAttendance(request.getWorkDate(), request.getEtcList(), empCode);
        if (message != null) {
            redirectAttributes.addFlashAttribute("error", message);
        }
        redirectAttributes.addFlashAttribute("message", "상신이 완료되었습니다.");

        redirectAttributes.addAttribute("attType", request.getEtcList().get(0).getAttType());
        redirectAttributes.addAttribute("workDate", request.getWorkDate());
        return "redirect:/att/etc/main";
    }
    @PostMapping("/delete")
    public String delete(@ModelAttribute EtcRequestDto request){
        attService.deleteAttendance(request.getEtcList());
        return "redirect:/att/etc/main";
    }
    @PostMapping("/requestCancel")
    public String requestCancel(@ModelAttribute EtcRequestDto request){
        attService.cancelAttendance(request.getEtcList());
        return "redirect:/att/etc/main";
    }

}

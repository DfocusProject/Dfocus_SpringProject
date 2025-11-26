package com.skuniv.dfocus_project.controller.att.request;

import com.skuniv.dfocus_project.CustomUserDetails;
import com.skuniv.dfocus_project.dto.AttEmpViewDto;
import com.skuniv.dfocus_project.dto.EtcRequestDto;
import com.skuniv.dfocus_project.dto.att.EtcSearchDto;
import com.skuniv.dfocus_project.service.AttService;
import com.skuniv.dfocus_project.service.EmpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/att/etc")
@SessionAttributes("etcSearchDto")  // ← ★ 검색조건 DTO를 세션에서 유지
public class EtcAttController {

    private final EmpService empService;
    private final AttService attService;

    /**
     * 세션에 etcSearchDto가 없으면 자동 생성
     */
    @ModelAttribute("etcSearchDto")
    public EtcSearchDto createSearchDto(Authentication authentication) {

        CustomUserDetails loginUser = (CustomUserDetails) authentication.getPrincipal();

        EtcSearchDto dto = new EtcSearchDto();
        dto.setWorkDate(LocalDate.now());

        // LEADER가 아니면 자신의 사번을 검색조건으로 고정
        if (!"LEADER".equals(loginUser.getRole())) {
            dto.setEmpCode(loginUser.getUsername());
        }

        return dto;
    }

    @GetMapping("/main")
    public String mainPage() {
        // etcSearchDto는 세션에서 자동 바인딩됨
        return "att/etc";
    }

    @GetMapping("/search")
    public String searchEtcAtt(
            @ModelAttribute("etcSearchDto") EtcSearchDto etcSearchDto,
            Model model,
            Authentication authentication
    ){
        CustomUserDetails loginUser = (CustomUserDetails) authentication.getPrincipal();
        String deptName = loginUser.getDeptName();

        String searchEmpCode;
        if ("LEADER".equals(loginUser.getRole())) {
            searchEmpCode = etcSearchDto.getEmpCode() != null ? etcSearchDto.getEmpCode() : "";
        } else {
            searchEmpCode = loginUser.getUsername();
        }

        List<AttEmpViewDto> empList =
                empService.getEtcAttEmpList(etcSearchDto.getPlanType(), etcSearchDto.getWorkDate(), searchEmpCode, deptName);

        for (AttEmpViewDto emp : empList) {
            emp.setRealWorkRecord(attService.getRealWorkRecord(emp.getEmpCode(), etcSearchDto.getWorkDate()));
        }

        model.addAttribute("empList", empList);
        return "att/etc";
    }

    @PostMapping("/save")
    public String saveAttendance(
            @ModelAttribute EtcRequestDto request,
            @ModelAttribute("etcSearchDto") EtcSearchDto etcSearchDto,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String applicant = userDetails.getUsername();

        String message = attService.saveEtcAttendance(
                request.getWorkDate(), request.getEtcList(), applicant);

        if (message != null) {
            redirectAttributes.addFlashAttribute("error", message);
        }

        return "redirect:/att/etc/search";  // 검색조건 유지
    }

    @PostMapping("/request")
    public String requestAttendance(
            @ModelAttribute EtcRequestDto request,
            @ModelAttribute("etcSearchDto") EtcSearchDto etcSearchDto,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        String empCode = userDetails.getUsername();
        String message = attService.requestEtcAttendance(
                request.getWorkDate(), request.getEtcList(), empCode);

        if (message != null) {
            redirectAttributes.addFlashAttribute("error", message);
        }
        redirectAttributes.addFlashAttribute("message", "상신이 완료되었습니다.");

        return "redirect:/att/etc/search";  // 검색조건 유지
    }

    @PostMapping("/delete")
    public String delete(
            @ModelAttribute EtcRequestDto request,
            @ModelAttribute("etcSearchDto") EtcSearchDto etcSearchDto) {

        attService.deleteAttendance(request.getEtcList());
        return "redirect:/att/etc/search";  // 검색조건 유지
    }

    @PostMapping("/requestCancel")
    public String requestCancel(
            @ModelAttribute EtcRequestDto request,
            @ModelAttribute("etcSearchDto") EtcSearchDto etcSearchDto) {

        attService.cancelAttendance(request.getEtcList());
        return "redirect:/att/etc/search"; // 검색조건 유지
    }

    /**
     * 필요할 경우 검색 조건 초기화하고 싶을 때 사용
     */
    @GetMapping("/reset")
    public String reset(SessionStatus sessionStatus) {
        sessionStatus.setComplete();   // 세션에서 DTO 제거
        return "redirect:/att/etc/main";
    }
}

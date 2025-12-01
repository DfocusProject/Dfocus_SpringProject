package com.skuniv.dfocus_project.controller.att.approval;

import com.skuniv.dfocus_project.CustomUserDetails;
import com.skuniv.dfocus_project.dto.approval.*;
import com.skuniv.dfocus_project.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@Controller
@RequiredArgsConstructor
@RequestMapping("/approval")
public class ApprovalController {
    private final ApprovalService approvalService;
    @GetMapping("/main")
    public String main(Model model) {
        ApprovalSearchDto approvalSearchDto = new ApprovalSearchDto();
        approvalSearchDto.setStartDate(LocalDate.now().withDayOfMonth(1));
        approvalSearchDto.setEndDate(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
        model.addAttribute("approvalSearchDto", approvalSearchDto);
        return "approval/main";
    }
    @GetMapping("/search")
    public String search(@ModelAttribute ApprovalSearchDto approvalSearchDto, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String loginEmpCode = userDetails.getUsername(); // getUsername() -> empCode
        ApprovalDocsResponse docsResponse = approvalService.searchDocs(approvalSearchDto, loginEmpCode);

        model.addAttribute("pendingDocsList", docsResponse.getPending());
        model.addAttribute("approvedDocsList", docsResponse.getApproved());
        model.addAttribute("rejectedDocsList", docsResponse.getRejected());
        model.addAttribute("approvalSearchDto", approvalSearchDto);
        return "approval/main";
    }

    @GetMapping("/detail/{requestId}")
    public String getDetail(@PathVariable Long requestId, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String loginEmpCode = userDetails.getUsername(); // getUsername() -> empCode
        EmpInfoDto empInfoDto = approvalService.getEmpInfo(requestId, loginEmpCode);
        ReqInfoDto reqInfoDto = approvalService.getReqInfoDto(requestId);
        CommuteInfoDto commuteInfoDto = approvalService.getCommuteInfoDto(empInfoDto.getReqDate(), empInfoDto.getTargetEmpCode());
        List<ApprovalInfoDto> approvalInfoDto = approvalService.getApprovalInfoDto(requestId);
        String rejectionReason = approvalService.getRejectedReason(requestId);
        model.addAttribute("empInfo", empInfoDto);
        model.addAttribute("reqInfo", reqInfoDto);
        model.addAttribute("commuteInfo", commuteInfoDto);
        model.addAttribute("approvalInfos", approvalInfoDto);
        model.addAttribute("requestId", requestId);
        model.addAttribute("rejectionReason", rejectionReason);  // 추가
        return "approval/detail :: detailFragment";
    }
    @PostMapping("/approve")
    public String approve(@RequestParam List<Long> requestIds, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String loginEmpCode = userDetails.getUsername(); // getUsername() -> empCode
        for(Long requestId : requestIds) {
            approvalService.approve(requestId, loginEmpCode);
        }
        return "redirect:/approval/main";
    }

    @PostMapping("/reject")
    public String reject(@RequestParam List<Long> requestIds, @RequestParam String reason, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String loginEmpCode = userDetails.getUsername(); // getUsername() -> empCode
        for(Long requestId : requestIds) {
            approvalService.reject(requestId, loginEmpCode, reason);
        }
        return "redirect:/approval/main";
    }
}

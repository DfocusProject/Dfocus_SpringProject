package com.skuniv.dfocus_project.controller.att.approval;

import com.skuniv.dfocus_project.CustomUserDetails;
import com.skuniv.dfocus_project.dto.approval.ApprovalDocsResponse;
import com.skuniv.dfocus_project.dto.approval.ApprovalSearchDto;
import com.skuniv.dfocus_project.dto.approval.DocumentDto;
import com.skuniv.dfocus_project.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequiredArgsConstructor
@RequestMapping("/approval")
public class ApprovalController {
    private final ApprovalService approvalService;
    @GetMapping("/main")
    public String main() {
        return "approval/main";
    }
    @GetMapping("/search")
    public String search(@RequestParam ApprovalSearchDto approvalSearchDto, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String loginEmpCode = userDetails.getUsername(); // getUsername() -> empCode
        ApprovalDocsResponse docsResponse = approvalService.searchDocs(approvalSearchDto, loginEmpCode);

        model.addAttribute("pendingDocsList", docsResponse.getPending());
        model.addAttribute("approvedDocsList", docsResponse.getApproved());
        model.addAttribute("rejectedDocsList", docsResponse.getRejected());

        return "approval/main";
    }
}

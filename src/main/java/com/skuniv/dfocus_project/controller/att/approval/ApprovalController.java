package com.skuniv.dfocus_project.controller.att.approval;

import com.skuniv.dfocus_project.CustomUserDetails;
import com.skuniv.dfocus_project.dto.approval.*;
import com.skuniv.dfocus_project.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


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
    public String search(@ModelAttribute ApprovalSearchDto approvalSearchDto, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String loginEmpCode = userDetails.getUsername(); // getUsername() -> empCode
        ApprovalDocsResponse docsResponse = approvalService.searchDocs(approvalSearchDto, loginEmpCode);

        model.addAttribute("pendingDocsList", docsResponse.getPending());
        model.addAttribute("approvedDocsList", docsResponse.getApproved());
        model.addAttribute("rejectedDocsList", docsResponse.getRejected());

        return "approval/main";
    }

    @GetMapping("/detail/{requestId}")
    public String getDetail(@PathVariable int requestId, Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String loginEmpCode = userDetails.getUsername(); // getUsername() -> empCode
        EmpInfoDto empInfoDto = approvalService.getEmpInfo(requestId, loginEmpCode);
//        ReqInfoDto reqInfoDto = approvalService.getReqInfoDto(requestId);
//        CommuteInfoDto commuteInfoDto = approvalService.getCommuteInfoDto(requestId);
//        ApprovalInfoDto approvalInfoDto = approvalService.getApprovalInfoDto(requestId);

        model.addAttribute("empInfo", empInfoDto);
//        model.addAttribute("reqInfoDto", reqInfoDto);
//        model.addAttribute("commuteInfo", commuteInfoDto);
//        model.addAttribute("approvalInfo", approvalInfoDto);


        return "approval/detail :: detailFragment";
    }

}

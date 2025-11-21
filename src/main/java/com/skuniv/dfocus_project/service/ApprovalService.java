package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.controller.att.approval.ApprovalDetailDto;
import com.skuniv.dfocus_project.dto.approval.*;
import com.skuniv.dfocus_project.mapper.ApprovalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ApprovalService {
    private final ApprovalMapper approvalMapper;
    public ApprovalDocsResponse searchDocs(ApprovalSearchDto approvalSearchDto, String loginEmpCode) {
        List<DocumentDto> allDocs = approvalMapper.getDocumentList(approvalSearchDto, loginEmpCode);
        Map<String, List<DocumentDto>> statusMap = allDocs.stream()
                .collect(Collectors.groupingBy(DocumentDto::getStatus));

        ApprovalDocsResponse response = new ApprovalDocsResponse();
        response.setPending(statusMap.getOrDefault("PENDING", Collections.emptyList()));
        response.setApproved(statusMap.getOrDefault("APPROVED", Collections.emptyList()));
        response.setRejected(statusMap.getOrDefault("REJECTED", Collections.emptyList()));

        return response;
    }

    public EmpInfoDto getEmpInfo(int requestId, String loginEmpCode) {
        return approvalMapper.getDetailEmpInfo(requestId, loginEmpCode);
    }

//    public ReqInfoDto getReqInfoDto(int requestId) {
//    }
//
//    public CommuteInfoDto getCommuteInfoDto(int requestId) {
//    }
//
//    public ApprovalInfoDto getApprovalInfoDto(int requestId) {
//    }
}

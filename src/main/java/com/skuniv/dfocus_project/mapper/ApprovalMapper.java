package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.dto.approval.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ApprovalMapper {
    //문서리스트 가져오기
    List<DocumentDto> getDocumentList(ApprovalSearchDto approvalSearchDto, String LoginEmpCode);

    EmpInfoDto getDetailEmpInfo(Long requestId, String loginEmpCode);

    CommuteInfoDto getDetailCommuteInfo(String reqDate, String resEmpCode);

    ReqInfoDto getReqInfo1(Long requestId);

    ReqInfoDto getReqInfo2(Long requestId);

    ReqInfoDto getReqInfo3(Long requestId);

    List<ApprovalInfoDto> getApprovalInfo(Long requestId);

    void updateApprovalLineStatus(String status, Long requestId, String empCode);

    void updateApprovalLineTime(Long requestId, String empCode);

    void updateAnnualCount(Long requestId, double count);

    double getAnnualRequestCount(Long requestId);

    void updateRejectReason(Long requestId, String reason);

    String getRejectedReason(Long requestId);
}

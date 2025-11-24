package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.dto.approval.*;
import com.skuniv.dfocus_project.mapper.ApprovalMapper;
import com.skuniv.dfocus_project.mapper.AttMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ApprovalService {
    private final ApprovalMapper approvalMapper;
    private final AttMapper attMapper;
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

    public EmpInfoDto getEmpInfo(Long requestId, String loginEmpCode) {
        return approvalMapper.getDetailEmpInfo(requestId, loginEmpCode);
    }

    public CommuteInfoDto getCommuteInfoDto(String reqDate, String resEmpCode) {
        return approvalMapper.getDetailCommuteInfo(reqDate, resEmpCode);
    }

    public ReqInfoDto getReqInfoDto(Long requestId) {
        String attType = attMapper.findAttTypeByrequestId(requestId);

        if (attType == null) {
            return null;
        }

        if(attType.equals("연장") || attType.equals("외출") || attType.equals("조퇴") || attType.equals("휴일")) {
            ReqInfoDto dto = approvalMapper.getReqInfo1(requestId);
            calculateHours(dto);
            return dto;
        } else if(attType.equals("반차")) {
            return approvalMapper.getReqInfo2(requestId);
        } else if(attType.equals("기타")) {
            return approvalMapper.getReqInfo3(requestId);
        } else {
            throw new IllegalStateException("Unknown attendance type: " + attType);
        }
    }

    public List<ApprovalInfoDto> getApprovalInfoDto(Long requestId) {
        return approvalMapper.getApprovalInfo(requestId);
    }

    private void calculateHours(ReqInfoDto dto) {
        if (dto.getStartTime() == null || dto.getEndTime() == null)
            return;

        LocalTime start = LocalTime.parse(dto.getStartTime());
        LocalTime end = LocalTime.parse(dto.getEndTime());

        // 날짜 보정
        if (Boolean.TRUE.equals(dto.getStartNextDay())) {
            start = start.plusHours(24);
        }
        if (Boolean.TRUE.equals(dto.getEndNextDay())) {
            end = end.plusHours(24);
        }

        double hours = Duration.between(start, end).toMinutes() / 60.0;
        dto.setHours(hours);
    }

    public void approve(Long requestId) {
        boolean isEtcOrNot = attMapper.isEtcTypeByRequestId(requestId);
        if (isEtcOrNot) {
            //실제 plan 변경 휴일 빼고
            attMapper.updateAttPlan(requestId);
        }
        //신청서 상태 변경
        attMapper.updateAttendanceStatus("APPROVED", requestId);
        //결재선 상태 변경
        approvalMapper.updateApprovalLineStatus("APPROVED", requestId);
    }

    public void reject(Long requestId) {
        //신청서 상태 변경
        attMapper.updateAttendanceStatus("REJECTED", requestId);
        //결재선 상태 변경
        approvalMapper.updateApprovalLineStatus("REJECTED", requestId);
    }
}

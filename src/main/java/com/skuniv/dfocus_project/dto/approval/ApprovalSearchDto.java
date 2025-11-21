package com.skuniv.dfocus_project.dto.approval;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ApprovalSearchDto {
    private LocalDate startDate; //조회 시작일자
    private LocalDate endDate;   //조회 종료일자
    private String empCode;      //대상자 사번
    private String reqType;      //근태 유형[연장/휴일/기타/조퇴외출반차 => OT, HT, ET, LT]
    private String reqDetailType;//신청 유형
    private String department;   //부서명
}

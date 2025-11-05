package com.skuniv.dfocus_project.dto;

import lombok.Getter;
import lombok.Setter;

//view로 넘겨 줄 조회 목록에 사용되는 사원 Dto
@Getter
@Setter
public class AttEmpDto {
    String empNo;
    String name;
    String position;
    String department;
    String plan;
    String workType;
    double expectedWorkHours;
    String realWorkRecord;
    String startTime;
    String endTime;
    //이미 저장되거나 상신된 사원 필드
    String reqReason;
    String reqReasonDetail;
    String reqStatus;
    String applicant;
    String reqStartTime;
    String reqEndTime;
}
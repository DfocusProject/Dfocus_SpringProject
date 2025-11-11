package com.skuniv.dfocus_project.dto;

import lombok.Getter;
import lombok.Setter;

//view로 넘겨 줄 조회 목록에 사용되는 사원 Dto
@Getter
@Setter
public class AttEmpViewDto extends BaseAttEmpDto{
    // 사원 정보
    private String name;
    private String position;
    private String department;
    private String planShiftType;
    //예상 근로 시간 및 실적
    private double expectedWorkHours;
    private String realWorkRecord;
    //신청자 사번
    private String applicant;

}
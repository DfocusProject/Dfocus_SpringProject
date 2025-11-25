package com.skuniv.dfocus_project.dto.att;

import lombok.Data;

@Data
public class DeptAttSearchResultDto {
    private String empCode;
    private String empName;
    private String position;
    private String department;
    private String planType;
    private String realType;
    private String workOnTime;
    private String workOffTime;
    private String holidayTime;
    private String overTime;
    private String expectedTime;
    private String remainTime;
}

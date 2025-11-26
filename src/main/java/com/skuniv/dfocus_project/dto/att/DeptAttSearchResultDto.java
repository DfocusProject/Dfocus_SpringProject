package com.skuniv.dfocus_project.dto.att;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DeptAttSearchResultDto {
    private String empCode;
    private String empName;
    private String position;
    private String department;
    private String planType;
    private String realType;
    private LocalTime workOnTime;
    private LocalTime workOffTime;
    private Double holidayTime;
    private Double overTime;
    private Double expectedTime;
    private Double remainTime;
}

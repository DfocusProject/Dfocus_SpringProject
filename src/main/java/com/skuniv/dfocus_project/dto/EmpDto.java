package com.skuniv.dfocus_project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmpDto {
    private String empCode;
    private String empName;
    private String positionName;
    private String deptCode;
    private String workPatternCode;
    private String status; //OFF, WORK
}

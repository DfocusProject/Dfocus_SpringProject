package com.skuniv.dfocus_project.dto.approval;

import lombok.Data;

@Data
public class EmpInfoDto {
    private String reqEmpCode;
    private String reqEmpName;
    private String targetEmpCode;
    private String targetEmpName;
    private String reqDate;
    private String planType;
    private String department;
}

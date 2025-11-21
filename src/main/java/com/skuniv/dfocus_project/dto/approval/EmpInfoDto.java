package com.skuniv.dfocus_project.dto.approval;

import lombok.Data;

@Data
public class EmpInfoDto {
    private String reqEmpCode;
    private String reqEmpName;
    private String resEmpCode;
    private String resEmpName;
    private String reqDate;
    private String planType;
    private String department;
}

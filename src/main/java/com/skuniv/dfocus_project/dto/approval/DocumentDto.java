package com.skuniv.dfocus_project.dto.approval;

import lombok.Data;

@Data
public class DocumentDto {
    private Long requestId;
    private String reqEmpCode;
    private String reqEmpName;
    private String targetEmpName;
    private String department;
    private String reqDetailType;
    private String reqDate;
    private String status;
}

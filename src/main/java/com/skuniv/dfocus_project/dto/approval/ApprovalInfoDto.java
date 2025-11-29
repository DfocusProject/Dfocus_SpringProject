package com.skuniv.dfocus_project.dto.approval;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ApprovalInfoDto {
    private int sequenceNo;
    private String department;
    private String duty;
    private String name;
    private String empCode;
    private String result;
    private LocalDate date;
    private String reason;
}

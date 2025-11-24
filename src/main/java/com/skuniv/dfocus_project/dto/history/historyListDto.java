package com.skuniv.dfocus_project.dto.history;

import lombok.Data;

import java.time.LocalDate;

@Data
public class historyListDto {
    private Long requestId;
    private LocalDate requestDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String targetEmpCode;
    private String targetEmpName;
    private String reqAttType;
    private String status;
    private String department;
}

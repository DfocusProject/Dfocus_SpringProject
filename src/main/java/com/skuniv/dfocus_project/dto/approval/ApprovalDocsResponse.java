package com.skuniv.dfocus_project.dto.approval;

import lombok.Data;

import java.util.List;

@Data
public class ApprovalDocsResponse {
    private List<DocumentDto> pending;
    private List<DocumentDto> approved;
    private List<DocumentDto> rejected;
}

package com.skuniv.dfocus_project.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ExistingEtcRequestDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private String etcType;      // "연차", "기타"
}

package com.skuniv.dfocus_project.dto.home;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DurationDto {
    private LocalDate startDate;
    private LocalDate endDate;
}

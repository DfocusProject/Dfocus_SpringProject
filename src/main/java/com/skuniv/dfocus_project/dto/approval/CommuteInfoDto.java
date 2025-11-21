package com.skuniv.dfocus_project.dto.approval;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CommuteInfoDto {
    private LocalTime workOnTime;
    private LocalTime workOffTime;
    private LocalDate workDate;
}

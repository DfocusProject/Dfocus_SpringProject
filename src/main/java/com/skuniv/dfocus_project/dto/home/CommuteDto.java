package com.skuniv.dfocus_project.dto.home;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CommuteDto {
    private LocalDate workDate;
    private LocalTime workOnTime;
    private LocalTime workOffTime;
}

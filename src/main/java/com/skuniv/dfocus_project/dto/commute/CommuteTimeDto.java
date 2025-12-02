package com.skuniv.dfocus_project.dto.commute;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CommuteTimeDto {
    private String empCode;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean startNextDay;
    private Boolean endNextDay;
}

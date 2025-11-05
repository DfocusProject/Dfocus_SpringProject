package com.skuniv.dfocus_project.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class CommuteRecordDto {
    private LocalTime startTime;
    private LocalTime endTime;
}

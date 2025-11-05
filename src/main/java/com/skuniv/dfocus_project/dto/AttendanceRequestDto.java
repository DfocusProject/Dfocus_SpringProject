package com.skuniv.dfocus_project.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class AttendanceRequestDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;  // LocalDate로 변경
    private List<AttendanceDto> attList;
}

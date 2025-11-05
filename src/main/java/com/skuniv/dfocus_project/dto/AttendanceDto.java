package com.skuniv.dfocus_project.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
//view에서 받는 용도
public class AttendanceDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;  // LocalDate로 변경
    private String empNo;
    private String workType;
    private String reason;
    private String reasonDetail;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String applicant;
    //private String expectedHours;
}

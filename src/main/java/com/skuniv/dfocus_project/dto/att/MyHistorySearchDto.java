package com.skuniv.dfocus_project.dto.att;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class MyHistorySearchDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String status;
    private String reqType;      //근태 유형[연장/휴일/기타/조퇴외출반차 => OT, HT, ET, LT]
    private String reqDetailType;//신청 유형
}

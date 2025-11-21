package com.skuniv.dfocus_project.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
//저장용 데이터로 쓰이며, 이를 상속해 전달용 DTO로 사용함
public class BaseAttEmpDto {
    private Long requestId;
    private String empCode;
    private String reason;
    private String reasonDetail;
    private String status;
    private Boolean startNextDay;
    private Boolean endNextDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean plannedStartNextDay;
    private Boolean plannedEndNextDay;
    private LocalTime plannedStartTime;
    private LocalTime plannedEndTime;
    private String attType;
    private String halfType;
    //etc
    private LocalDate startDate;
    private LocalDate endDate;
    private String planType;
    private String newShiftType;
    private Boolean isTodayRequest;
    private Double balanceDay;
}

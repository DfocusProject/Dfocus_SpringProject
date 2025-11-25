package com.skuniv.dfocus_project.dto.approval;

import lombok.Data;

@Data
public class ReqInfoDto {
    private String startDate;
    private String endDate;
    private String reason;
    private String reasonDetail;
    private Boolean startNextDay;
    private String startTime;
    private Boolean endNextDay;
    private String endTime;
    private double hours;
    private String halfType;
    private String reqDetailType;
    private String reqType;    //[연차, 연장, 조출, 휴일, 조퇴, 외출, 반차, 기타]
    private Boolean isTodayRequest;
}

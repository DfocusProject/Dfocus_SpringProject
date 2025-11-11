package com.skuniv.dfocus_project.dto;

import lombok.Data;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 근무 Shift
 * over2_start_time, over_end_time이 있으면 계획에 이미 조출/연장 포함됨
 */
@Data
public class Shift {
    private String shiftCode;
    private String shiftName;
    private String timeItemCode;
    private String memo;
    private String useYn;
    private String fiveWeekYn;
    private String workTypeCode;
    private String workDayType;

    // 기본 출근
    private LocalTime workOnTime;                // work_on_time (기본 출근)

    // 기본 퇴근
    private LocalTime workOffTime;               // work_off_time (기본 퇴근)
    private Boolean workOffNextDay;              // work_off_next_day

    // 휴게 시작
    private LocalTime break1StartTime;           // break1_start_time
    private Boolean break1StartNextDay;          // break1_start_next_day

    // 휴게 종료
    private LocalTime break1EndTime;             // break1_end_time
    private Boolean break1EndNextDay;            // break1_end_next_day

    // 조출 시작 (계획된 조출)
    private LocalTime over2StartTime;            // over2_start_time
    private Boolean over2StartNextDay;           // over2_start_next_day

    // 조출 종료
    private LocalTime over2EndTime;              // over2_end_time
    private Boolean over2EndNextDay;             // over2_end_next_day

    // 연장 시작
    private LocalTime overStartTime;             // over_start_time
    private Boolean overStartNextDay;            // over_start_next_day

    // 연장 종료 (계획된 연장)
    private LocalTime overEndTime;               // over_end_time
    private Boolean overEndNextDay;              // over_end_next_day


    // ============ 중요! 실제 계획된 시간 ============

    /**
     * 실제 계획된 출근 시간
     * - over2_start_time이 있으면 조출 시간
     * - 없으면 기본 출근 시간
     */
    public LocalTime getPlannedStartTime() {
        return over2StartTime != null ? over2StartTime : workOnTime;
    }

    /**
     * 실제 계획된 출근의 익일 여부
     */
    public Boolean getPlannedStartNextDay() {
        return over2StartTime != null ? over2StartNextDay : false;
    }

    /**
     * 실제 계획된 퇴근 시간
     * - over_end_time이 있으면 연장 시간
     * - 없으면 기본 퇴근 시간
     */
    public LocalTime getPlannedEndTime() {
        return overEndTime != null ? overEndTime : workOffTime;
    }

    /**
     * 실제 계획된 퇴근의 익일 여부
     */
    public Boolean getPlannedEndNextDay() {
        return overEndTime != null ? overEndNextDay : workOffNextDay;
    }

    /**
     * 계획된 총 근무시간 (분) - 조출/연장 포함
     */
    public long getPlannedWorkMinutes() {
        LocalTime start = getPlannedStartTime();
        LocalTime end = getPlannedEndTime();
        Boolean startNextDay = getPlannedStartNextDay();
        Boolean endNextDay = getPlannedEndNextDay();

        if (start == null || end == null) {
            return 0;
        }

        int startMinutes = start.getHour() * 60 + start.getMinute();
        int endMinutes = end.getHour() * 60 + end.getMinute();

        if (Boolean.TRUE.equals(startNextDay)) {
            startMinutes += 1440;
        }
        if (Boolean.TRUE.equals(endNextDay)) {
            endMinutes += 1440;
        }

        return endMinutes - startMinutes;
    }
    public long getBreakMinutes() {

        int startMinutes = break1StartTime.getHour() * 60 + break1StartTime.getMinute();
        int endMinutes = break1EndTime.getHour() * 60 + break1EndTime.getMinute();

        // 자정 넘김 처리
        if (Boolean.TRUE.equals(break1StartNextDay)) {
            startMinutes += 1440;
        }
        if (Boolean.TRUE.equals(break1EndNextDay)) {
            endMinutes += 1440;
        }

        return endMinutes - startMinutes;
    }

}
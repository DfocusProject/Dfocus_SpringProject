package com.skuniv.dfocus_project.domain.Time;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class TimeRange {
    private LocalDate baseDate;   // 기준 날짜 (예: 근무일자)
    private LocalTime startTime;  // 시작 시각
    private Boolean startNextDay; // 시작이 익일인가
    private LocalTime endTime;    // 종료 시각
    private Boolean endNextDay;   // 종료가 익일인가

    /** 실제 시작 시각 */
    public LocalDateTime getStartDateTime() {
        if (baseDate == null || startTime == null) {
            return null;
        }
        long plus = (startNextDay != null && startNextDay) ? 1 : 0;
        return baseDate.plusDays(plus).atTime(startTime);
    }

    /** 실제 종료 시각 */
    public LocalDateTime getEndDateTime() {
        if (baseDate == null || endTime == null) {
            return null;
        }
        long plus = (endNextDay != null && endNextDay) ? 1 : 0;
        return baseDate.plusDays(plus).atTime(endTime);
    }

    /** 총 지속 시간 (분) */
    public long getDurationMinutes() {
        return Duration.between(getStartDateTime(), getEndDateTime()).toMinutes();
    }

    /** 다른 TimeRange와 겹치는가 */
    public boolean overlaps(TimeRange other) {
        LocalDateTime start = getStartDateTime();
        LocalDateTime end = getEndDateTime();
        LocalDateTime oStart = other.getStartDateTime();
        LocalDateTime oEnd = other.getEndDateTime();

        return !start.isAfter(oEnd) && !end.isBefore(oStart);
    }
    public boolean contains(TimeRange other) {
        return !other.getStartDateTime().isBefore(this.getStartDateTime()) &&
                !other.getEndDateTime().isAfter(this.getEndDateTime());
    }
}

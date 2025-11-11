package com.skuniv.dfocus_project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeRecordDto {
    private LocalTime startTime;
    private LocalTime endTime;

    public long getDurationMinutes() {
        return Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * 다른 시간 범위와 겹치는지 확인
     */
    public boolean overlaps(TimeRecordDto other) {
        return !this.endTime.isBefore(other.startTime) &&
                !other.endTime.isBefore(this.startTime);
    }

    /**
     * 다른 시간 범위와의 교집합 반환
     * 겹치지 않으면 null 반환
     */
    public TimeRecordDto getOverlap(TimeRecordDto other) {
        if (!overlaps(other)) {
            return null;
        }

        LocalTime overlapStart = this.startTime.isAfter(other.startTime)
                ? this.startTime
                : other.startTime;

        LocalTime overlapEnd = this.endTime.isBefore(other.endTime)
                ? this.endTime
                : other.endTime;

        return new TimeRecordDto(overlapStart, overlapEnd);
    }
}

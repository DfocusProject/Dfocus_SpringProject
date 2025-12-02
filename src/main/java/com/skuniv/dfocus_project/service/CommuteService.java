package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.dto.commute.CommuteTimeDto;
import com.skuniv.dfocus_project.mapper.CommuteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime; // LocalDateTime 추가
import java.time.Duration;      // Duration 추가

@Service
@RequiredArgsConstructor
public class CommuteService {
    private final CommuteMapper commuteMapper;

    // setWorkOnTime 메서드는 그대로 유지
    public String setWorkOnTime(String username) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (commuteMapper.isAlreadyCommute(username, today)) {
            System.out.println("이미 출근처리 됨");
            return "이미 출근 처리 되었습니다.";
        }
        // 야간조를 위해 workDate는 today로, startTime은 now로 저장 (익일 퇴근 시에도 이 workDate가 사용됨)
        commuteMapper.setWorkOnTime(username, today, now);
        return "출근 처리 되었습니다.";
    }

    public String setWorkOffTime(String username) {
        CommuteTimeDto time = commuteMapper.getLatestWorkOnTime(username);
        LocalTime now = LocalTime.now();

        if (time == null || time.getEndTime() != null) {
            return "처리할 출근 기록이 존재하지 않거나 이미 퇴근 처리되었습니다.";
        }

        // 출근 datetime
        LocalDateTime checkInDateTime = time.getWorkDate().atTime(time.getStartTime());
        // 현재 datetime
        LocalDateTime checkOutDateTime = LocalDateTime.now();

        // 근무시간 검증
        Duration duration = Duration.between(checkInDateTime, checkOutDateTime);
        if (duration.isNegative() || duration.toHours() > 18) {
            return "근무 시간이 너무 오래되어 퇴근 처리가 불가능합니다. (경과 시간: " + duration.toHours() + "시간)";
        }

        // ───────────────────────────
        // ★★ 출근 날짜와 퇴근 날짜 비교하여 startEndDay 설정 ★★
        // 출근일(workDate) < 퇴근일(LocalDate.now()) → 다음날 퇴근
        // ───────────────────────────
        boolean isNextDay = checkOutDateTime.toLocalDate().isAfter(time.getWorkDate());
        time.setEndNextDay(isNextDay);

        // 퇴근 시간(LocalTime) 기록
        time.setEndTime(now);

        // DB 반영
        commuteMapper.setWorkOffTime(time);

        return "퇴근 처리 되었습니다. 다음날 퇴근 여부: " +
                (isNextDay ? "예 (startEndDay=true)" : "아니오 (startEndDay=false)");
    }
}
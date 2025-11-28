package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.domain.Time.TimeRange;
import com.skuniv.dfocus_project.dto.BaseAttEmpDto;
import com.skuniv.dfocus_project.mapper.AttMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceValidateService {

    private final AttMapper attMapper;

    public String validate(BaseAttEmpDto dto, LocalDate workDate, String realWorkRecord, TimeRange expectedWorkTime, double weeklyWorkHours) {
        String result = null;

        String attType = dto.getAttType();
        String empCode = dto.getEmpCode();
        long startMinutes = dto.getStartTime().toSecondOfDay() / 60;
        long endMinutes = dto.getEndTime().toSecondOfDay() / 60;

        // 익일 근무라면 24시간 추가
        if (dto.getEndNextDay()) {
            endMinutes += 1440;
        }

        long workHours = (endMinutes - startMinutes) / 60;

        if (workHours + weeklyWorkHours > 52) {
            return "주 예상 근로 시간이 52시간을 초과합니다";
        }

        if (!attType.equals("휴일") && "결근".equals(realWorkRecord)) {
            return "결근 상태에는 신청 불가";
        }
        if (realWorkRecord.equals("연차")) {
            return "연차 사용 시 일반 근태 신청이 불가합니다.";
        }
        // 0. 연차 존재 여부 체크: 다른 신청 불가

        int existingAnnualLeave = attMapper.existsAttendanceRequest(empCode, workDate, "연차");
        if (existingAnnualLeave > 0) {
            boolean approved = attMapper.isApproved(empCode, workDate, "연차");
            return approved
                    ? "이미 승인된 연차 내역 존재"
                    : "이미 연차 신청 내역 존재";
        }

        // 2. 타입별 검증
        if ("연장".equals(attType) || "조출".equals(attType)) {
            return validateOvertime(dto, workDate);
        }
        if ("반차".equals(attType) || "조퇴".equals(attType)) {
            result = validateTimeConflict(dto, workDate);
        }
        if ("외출".equals(attType) || "조퇴".equals(attType)) {
            TimeRange requestTime = new TimeRange(workDate,
                    dto.getStartTime(), dto.getStartNextDay(),
                    dto.getEndTime(), dto.getEndNextDay());
            System.out.println("requestTime = " + requestTime);
            System.out.println("expectedWorkTime = " + expectedWorkTime);
            if (!expectedWorkTime.contains(requestTime)) {
                result = "근무시간이 아닙니다.";
            }
        }
        return result;
    }

    private String validateOvertime(BaseAttEmpDto dto, LocalDate workDate) {
        String empCode = dto.getEmpCode();
        String attType = dto.getAttType();

        // 2. 시간 범위 체크
        TimeRange requestTime = new TimeRange(workDate,
                dto.getStartTime(), dto.getStartNextDay(),
                dto.getEndTime(), dto.getEndNextDay());

        TimeRange allowedTimeRange = attMapper.getAllowedTimeRange(empCode, workDate, attType);

        if (allowedTimeRange != null
                && allowedTimeRange.getStartTime() != null
                && allowedTimeRange.getEndTime() != null
                && !allowedTimeRange.contains(requestTime)) {
            return attType + " 신청 허용 범위 초과";
        }

        List<String> conflictTypes = "연장".equals(attType)
                ? List.of("연차", "조퇴")
                : List.of("연차");

        for (String conflictType : conflictTypes) {

            int count = attMapper.existsAttendanceRequest(empCode, workDate, conflictType);
            if (count > 0) {
                return "이미 " + conflictType + " 신청 내역 존재";
            }
        }
        int exists = attMapper.existsHalfDayRequest(empCode, workDate, "전반차");
        if (exists == 1 && "조출".equals(attType)) {
            return "이미 전반차 신청이 존재합니다.";
        }

        exists = attMapper.existsHalfDayRequest(empCode, workDate, "후반차");
        if (exists == 1 && "연장".equals(attType)) {
            return "이미 후반차 신청이 존재합니다.";
        }

        return null;
    }

    private String validateTimeConflict(BaseAttEmpDto dto, LocalDate workDate) {
        String empCode = dto.getEmpCode();

        // 요청 시간
        TimeRange requestTime = new TimeRange(workDate,
                dto.getStartTime(), dto.getStartNextDay(),
                dto.getEndTime(), dto.getEndNextDay());

        // 시간 겹칠 수 있는 타입들
        List<String> timeConflictTypes = List.of("조퇴", "반차");

        List<BaseAttEmpDto> existingRequests = attMapper.getAttendanceRequestsByDate(
                empCode, workDate, timeConflictTypes
        );

        for (BaseAttEmpDto existing : existingRequests) {

            if (dto.getRequestId() != null &&
                    dto.getRequestId().equals(existing.getRequestId())) {
                continue;
            }

            TimeRange existingTime = new TimeRange(workDate,
                    existing.getStartTime(), existing.getStartNextDay(),
                    existing.getEndTime(), existing.getEndNextDay());

            // 시간 겹침 체크
            if (requestTime.overlaps(existingTime)) {
                return "이미 신청된 " + existing.getAttType() +
                        "(" + existing.getStartTime() + "-" + existing.getEndTime() +
                        ")와 시간이 겹칩니다";
            }
        }

        return null;
    }
}
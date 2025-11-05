package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.dto.AttEmpDto;
import com.skuniv.dfocus_project.dto.AttendanceDto;
import com.skuniv.dfocus_project.dto.CommuteRecordDto;
import com.skuniv.dfocus_project.mapper.AttMapper;
import com.skuniv.dfocus_project.mapper.PatternMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class AttService {

    private final AttMapper attMapper;
    private final PatternMapper patternMapper;
//    // 주간 예상 근무 시간 계산
//    public Double getWeeklyExpectedHours(String empCode, LocalDate selectedDate) {
//
//        LocalDate today = LocalDate.now();
//        LocalDate startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
//        LocalDate endOfWeek = selectedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
//
//        double totalHours = 0.0;
//        double totalLateHours = 0.0;
//
//        for (LocalDate date = startOfWeek; !date.isAfter(endOfWeek); date = date.plusDays(1)) {
//
//            // 계획된 출퇴근 시간
//            Map<String, LocalTime> plan = attMapper.getCommuteHours(empCode, date);
//            if (plan == null) continue;
//            LocalTime startTime = plan.get("start_time");
//            LocalTime endTime = plan.get("end_time");
//            if (startTime == null || endTime == null) continue;
//
//            double fullDayHours = Duration.between(startTime, endTime).toMinutes() / 60.0;
//
//            // 근태 신청 반영
//            startTime = applyRequestStart(empCode, date, startTime);
//            endTime = applyRequestEnd(empCode, date, endTime, fullDayHours);
//
//            // 외출 시간 반영
//            double outHours = calculateOutHours(empCode, date);
//
//            // 하루 총 근무 시간
//            double dayHours = Duration.between(startTime, endTime).toMinutes() / 60.0 - outHours;
//            totalHours += dayHours;
//
//            // 과거·오늘 날짜는 실제 근무 기록 반영
//            if (!date.isAfter(today)) {
//                Map<String, LocalTime> actual = attMapper.getActualCommuteRecord(empCode, date);
//                if (actual == null) {
//                    totalLateHours += dayHours; // 결근 처리
//                    continue;
//                }
//
//                LocalTime actualStart = actual.get("start_time");
//                LocalTime actualEnd = actual.get("end_time");
//
//                if (actualStart != null && actualStart.isAfter(startTime)) {
//                    totalLateHours += Duration.between(startTime, actualStart).toMinutes() / 60.0;
//                }
//                if (actualEnd != null && actualEnd.isBefore(endTime)) {
//                    totalLateHours += Duration.between(actualEnd, endTime).toMinutes() / 60.0;
//                }
//            }
//        }
//
//        return totalHours - totalLateHours;
//    }

    // 실적 조회
    public String getRealWorkRecord(String empCode, LocalDate workDate) {
        //현재 시각
        LocalDateTime now = LocalDateTime.now();
        //계획 근태
        String plannedShift = attMapper.getPlannedShift(empCode, workDate);
        System.out.println("plannedShift : " + plannedShift);
        //계획 근태의 출근 시간 조회
        Map<String, String> result = attMapper.getPlannedCommuteTime(plannedShift);

        String startTimeStr = result.get("start_time");  // DB에서 가져온 값
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        LocalTime startTime = LocalTime.parse(startTimeStr, formatter);

        LocalDateTime plannedStartDateTime = LocalDateTime.of(workDate, startTime);
        String plannedShiftName = attMapper.getShiftName(plannedShift);
        //실제 출근 기록
        CommuteRecordDto commuteRecord = getCommuteRecordDto(empCode, workDate);

        if (now.isBefore(plannedStartDateTime)) {
            return "-";
        } else if (commuteRecord != null) {
            return plannedShiftName; // 또는 "정상 출근"
        } else return "결근";
    }

    public void recordOnCommute(String empCode, LocalDate today, LocalTime now) {
        attMapper.addRecordOnCommute(empCode, today, now);
    }

    public void recordOffCommute(String empCode, LocalDate today, LocalTime now) {
        attMapper.addRecordOffCommute(empCode, today, now);
    }

    public String saveAttendance(LocalDate workDate, List<AttendanceDto> attList, String loginEmpCode) {
        for (AttendanceDto dto : attList) {

            String validationError = validateConflicts(dto, workDate);
            if (validationError != null) {
                return validationError; // 컨트롤러로 에러 메시지 전달
            }

            dto.setWorkDate(workDate);
            dto.setStatus("SAVED");
            dto.setApplicant(loginEmpCode);

            if (attMapper.existsAttendanceRequest(dto.getEmpNo(), workDate, dto.getWorkType()) != 0) {
                attMapper.updateAttendanceRequest(dto);
            } else {
                attMapper.insertAttendanceRequest(dto);
            }
        }
        return null; // 에러 없이 성공
    }

    // 검증 로직 분리
    private String validateConflicts(AttendanceDto dto, LocalDate workDate) {

        String empNo = dto.getEmpNo();
        String workType = dto.getWorkType();
        String realWorkRecord = getRealWorkRecord(empNo, workDate);
        if (realWorkRecord.equals("결근")) {
            System.out.println("realWorkRecord : " + realWorkRecord);
            return "사번: " + empNo + " 은(는) 결근 처리된 상태입니다.";
        }
        // 검증 규칙 매핑
        Map<String, List<String>> conflictRules = Map.of(
                "연장", List.of("연차", "반차", "조퇴", "휴가"),
                "조출", List.of("연차", "휴가"),
                "조퇴", List.of("연차", "휴가"),
                "외출", List.of("연차", "휴가"),
                "반차", List.of("연차", "휴가")
        );

        List<String> conflicts = conflictRules.get(workType);
        if (conflicts != null) {
            for (String conflictType : conflicts) {
                if (attMapper.existsAttendanceRequest(empNo, workDate, conflictType) != 0) {
                    return "사번: " + empNo + " 에는 이미 " + conflictType + " 신청 내역이 존재합니다.";
                }
            }
        }

        return null; // 문제 없음
    }

//    // ==================== 보조 메서드 ====================
//
//    private LocalTime applyRequestStart(String empCode, LocalDate date, LocalTime startTime) {
//        // 휴가, 조기출근 반영
//        Map<String, String> holiday = attMapper.getRequestHours(empCode, date, "holiday");
//        if (holiday != null && holiday.get("start_time") != null) {
//            startTime = parseHHmm(holiday.get("start_time"));
//        }
//        Map<String, String> early = attMapper.getRequestHours(empCode, date, "early");
//        if (early != null && early.get("start_time") != null) {
//            startTime = parseHHmm(early.get("start_time"));
//        }
//        // 오전 반차
//        Map<String, String> halfM = attMapper.getRequestHours(empCode, date, "half_m");
//        if (halfM != null) {
//            double halfHours = Duration.between(startTime, LocalTime.NOON).toMinutes() / 60.0;
//            startTime = startTime.plusMinutes((long)(halfHours * 60));
//        }
//        return startTime;
//    }
//
//    private LocalTime applyRequestEnd(String empCode, LocalDate date, LocalTime endTime, double fullDayHours) {
//        // 휴가, OT, 조기퇴근 반영
//        Map<String, String> holiday = attMapper.getRequestHours(empCode, date, "holiday");
//        if (holiday != null && holiday.get("end_time") != null) {
//            endTime = parseHHmm(holiday.get("end_time"));
//        }
//        Map<String, String> ot = attMapper.getRequestHours(empCode, date, "ot");
//        if (ot != null && ot.get("end_time") != null) {
//            endTime = parseHHmm(ot.get("end_time"));
//        }
//        Map<String, String> earlyLeave = attMapper.getRequestHours(empCode, date, "early_leave");
//        if (earlyLeave != null && earlyLeave.get("end_time") != null) {
//            endTime = parseHHmm(earlyLeave.get("end_time"));
//        }
//        // 오후 반차
//        Map<String, String> halfA = attMapper.getRequestHours(empCode, date, "half_a");
//        if (halfA != null) {
//            endTime = endTime.minusMinutes((long)(fullDayHours * 30)); // 0.5일치 차감
//        }
//        return endTime;
//    }
//
//    private double calculateOutHours(String empCode, LocalDate date) {
//        Map<String, String> out = attMapper.getRequestHours(empCode, date, "out");
//        if (out == null) return 0.0;
//        LocalTime startOut = out.get("start_time") != null ? parseHHmm(out.get("start_time")) : null;
//        LocalTime endOut = out.get("end_time") != null ? parseHHmm(out.get("end_time")) : null;
//        if (startOut != null && endOut != null) {
//            return Duration.between(startOut, endOut).toMinutes() / 60.0;
//        }
//        return 0.0;
//    }
//
//    private LocalTime parseHHmm(String timeStr) {
//        if (timeStr == null || timeStr.length() != 4) return null;
//        int hour = Integer.parseInt(timeStr.substring(0, 2));
//        int minute = Integer.parseInt(timeStr.substring(2, 4));
//        return LocalTime.of(hour, minute);
//    }
        public CommuteRecordDto getCommuteRecordDto(String empCode, LocalDate workDate) {
            CommuteRecordDto commuteRecord = attMapper.getActualCommuteRecord(empCode, workDate);
            return commuteRecord;
        }

        public Map<String, String> getPlannedCommuteTime(String plannedShift){
            return attMapper.getPlannedCommuteTime(plannedShift);
        }

    public String getShiftCodeByShiftName(String plan) {
        return patternMapper.findShiftCodeByShiftName(plan);
    }
    //신청 내역이 존재하는 신청 정보
    public AttEmpDto getSavedAttendance(String code, LocalDate workDate, String attType) {
        return attMapper.findAttendanceRecord(code, workDate, attType);
    }

    public void deleteAttendance(LocalDate workDate, List<AttendanceDto> attList) {
        for (AttendanceDto dto : attList) {
            dto.setWorkDate(workDate);
            attMapper.deleteAttendanceRecord(dto);
        }
    }
}

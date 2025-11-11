package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.domain.Time.TimeRange;
import com.skuniv.dfocus_project.dto.*;
import com.skuniv.dfocus_project.mapper.AttMapper;
import com.skuniv.dfocus_project.mapper.DeptMapper;
import com.skuniv.dfocus_project.mapper.PatternMapper;
import com.skuniv.dfocus_project.mapper.ShiftMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class AttService {

    private final AttMapper attMapper;
    private final PatternMapper patternMapper;
    private final DeptMapper deptMapper;
    private final ShiftMapper shiftMapper;

    // 실적 조회
    public String getRealWorkRecord(String empCode, LocalDate workDate) {
        //현재 시각
        LocalDateTime now = now();
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
        TimeRecordDto actualCommuteRecord = attMapper.getActualCommuteRecord(empCode, workDate);

        if (now.isBefore(plannedStartDateTime)) {
            return "-";
        } else if (actualCommuteRecord != null) {
            return plannedShiftName; // 또는 "정상 출근"
        } else return "결근";
    }

    public void recordOnCommute(String empCode, LocalDate today, LocalTime now) {
        attMapper.addRecordOnCommute(empCode, today, now);
    }

    public void recordOffCommute(String empCode, LocalDate today, LocalTime now) {
        attMapper.addRecordOffCommute(empCode, today, now);
    }

    public String saveAttendance(LocalDate workDate, List<BaseAttEmpDto> attList, String loginEmpCode) {
        StringBuilder warningMessages = new StringBuilder(); // 저장은 되지만 유의해야 할 사항
        StringBuilder fatalErrors = new StringBuilder(); // 저장 불가 (ex. 상신 후 수정 불가)
        StringBuilder resultMessages = new StringBuilder(); // 저장/수정 결과 메시지

        for (BaseAttEmpDto dto : attList) {
            System.out.println("dto.getRequestId(): " + dto.getRequestId());
            if ("상신".equals(dto.getStatus())) {
                fatalErrors.append("사번(").append(dto.getEmpCode())
                        .append(") : 이미 상신처리 되어, 수정이 불가합니다.\n");
                continue; // 저장 안 함
            }

            String validationError = validateConflicts(dto, workDate);
            dto.setStatus("SAVED");
            if ("반차".equals(dto.getAttType())) {
                TimeRange planTime = attMapper.getPlannedCommuteTime2(workDate, dto.getEmpCode());
                LocalDateTime start = planTime.getStartDateTime();
                LocalDateTime end = planTime.getEndDateTime();

                if ("morningOff".equals(dto.getHalfType())) {
                    // 오전반차 → 근무 후반부만 남음
                    start = start.plusHours(4);
                } else if ("afternoonOff".equals(dto.getHalfType())) {
                    // 오후반차 → 근무 전반부만 남음
                    end = start.plusHours(4);
                }

                dto.setStartTime(start.toLocalTime());
                dto.setEndTime(end.toLocalTime());
                dto.setStartNextDay(!start.toLocalDate().equals(workDate));
                dto.setEndNextDay(!end.toLocalDate().equals(workDate));

            }
                try {
                    boolean isUpdate = dto.getRequestId() != null;
                    System.out.println("isUpdate = " + isUpdate);

                    if (isUpdate) {
                        attMapper.updateAttendanceRequest(dto, loginEmpCode, workDate);
                        attMapper.updateAttendanceRequestGeneral(dto);
                        resultMessages.append("사번(").append(dto.getEmpCode())
                                .append(") : 근태 신청 수정 완료\n");
                    } else {
                        attMapper.insertAttendanceRequest(dto, loginEmpCode, workDate);
                        attMapper.insertAttendanceRequestGeneral(dto);
                        resultMessages.append("사번(").append(dto.getEmpCode())
                                .append(") : 근태 신청 저장 완료\n");
                    }

                    if (validationError != null) {
                        warningMessages
                                .append(validationError)
                                .append("\n");
                    }

                } catch (Exception e) {
                    warningMessages.append(dto.getEmpCode())
                            .append(" : 저장 중 오류 발생 (")
                            .append(e.getMessage())
                            .append(")\n");
                    System.out.println("e = " + e);
                }
            }
            StringBuilder finalMessage = new StringBuilder("===처리 결과===\n\n");
            if (fatalErrors.length() > 0) {
                finalMessage.append(fatalErrors).append("\n");
            }

            if (resultMessages.length() > 0) {
                finalMessage.append(resultMessages).append("\n");
            }


            if (warningMessages.length() > 0) {
                finalMessage.append("유의 사항:\n").append(warningMessages);
            }

            if (fatalErrors.length() == 0 && warningMessages.length() == 0) {
                return "모든 항목이 성공적으로 저장되었습니다.";
            }

        return finalMessage.toString();
        }


    // 검증 로직 분리
    private String validateConflicts(BaseAttEmpDto dto, LocalDate workDate) {

        String empCode = dto.getEmpCode();
        String attType = dto.getAttType();
        String planShiftType = attMapper.getPlannedShift(empCode, workDate);
        String realWorkRecord = getRealWorkRecord(empCode, workDate);
        if (realWorkRecord.equals("결근")) {
            return "사번(" + empCode + "): 결근 상태";
        }
        // 휴일 근무 시간 검증
        if (("연장".equals(attType) && "휴일".equals(planShiftType)) || ("조출".equals(attType) && "휴일".equals(planShiftType))) {
            LocalTime startTime = dto.getStartTime();
            LocalTime endTime = dto.getEndTime();

            long hoursWorked = Duration.between(startTime, endTime).toHours();
            if (hoursWorked < 8) {
                return "휴일 근무 8시간 미만 신청 시 연장 근무 신청이 불가합니다.";
            }
        }
        // 검증 규칙 매핑
        Map<String, List<String>> conflictRules = Map.of(
                "연장", List.of("연차", "반차", "조퇴", "휴가"),
                "조출", List.of("연차", "휴가"),
                "조퇴", List.of("연차", "휴가"),
                "외출", List.of("연차", "휴가"),
                "반차", List.of("연차", "휴가")
        );

        List<String> conflicts = conflictRules.get(attType);
        if (conflicts != null) {
            for (String conflictType : conflicts) {
                if (attMapper.existsAttendanceRequest(empCode, workDate, conflictType) != 0) {
                    return "이미 " + conflictType + " 신청 내역이 존재합니다.";
                }
            }
        }

        return null; // 문제 없음
    }

    public TimeRange getPlannedCommuteTime(String empCode, LocalDate workDate) {
        return attMapper.getPlannedCommuteTime2(workDate, empCode);
    }

    public void deleteAttendance(List<BaseAttEmpDto> attList) {
        for (BaseAttEmpDto dto : attList) {
            if(dto.getRequestId() != null) {
                attMapper.deleteAttendanceRecord(dto);
            }
        }
    }

    public String requestAttendance(LocalDate workDate, List<BaseAttEmpDto> attList, String loginEmpCode) {
        // 1. 저장만 수행 (메시지는 반환하지 않음)
        saveAttendance(workDate, attList, loginEmpCode);

        // 2. 상신 검증 및 결과 메시지
        StringBuilder requestMessages = new StringBuilder();
        StringBuilder fatalErrors = new StringBuilder();

        for (BaseAttEmpDto dto : attList) {
            // validation 체크
            String validationError = validateConflicts(dto, workDate);
            if (validationError != null) {
                fatalErrors.append(dto.getEmpCode())
                        .append(" : 상신 불가 - ")
                        .append(validationError)
                        .append("\n");
                continue;
            }

            // 이미 상신된 경우
            Long requestId = attMapper.findAttendanceRequestId(dto.getEmpCode(), dto.getAttType(), workDate);
            if (attMapper.existRequestRecord(requestId)) {
                fatalErrors.append(dto.getEmpCode())
                        .append(" : 이미 상신되었습니다.\n");
                continue;
            }

            // 결재선 생성
            DeptDto dept = deptMapper.getDeptByEmpCode(dto.getEmpCode());
            String leader = deptMapper.getLeaderByDeptCode(dept.getDeptCode());

            // 본인 결재선
            attMapper.insertApprovalRecord(dto.getEmpCode(), dto.getEmpCode(), requestId, 1, "REQUESTED", now());

            // 리더 결재선
            if (loginEmpCode.equals(leader)) {
                attMapper.insertApprovalRecord(dto.getEmpCode(), leader, requestId, 2, "APPROVED", now());
                attMapper.updateAttendanceStatus("APPROVED", requestId);
            } else {
                attMapper.insertApprovalRecord(dto.getEmpCode(), leader, requestId, 2, "PENDING", null);
                attMapper.updateAttendanceStatus("REQUESTED", requestId);
            }

            requestMessages.append(dto.getEmpCode())
                    .append(" : 상신 완료\n");
        }

        // 3. 최종 메시지 조합
        StringBuilder finalMessage = new StringBuilder();
        if (requestMessages.length() > 0) {
            finalMessage.append("상신 결과:\n").append(requestMessages).append("\n");
        }
        if (fatalErrors.length() > 0) {
            finalMessage.append("상신 불가 내역:\n").append(fatalErrors).append("\n");
        }

        return finalMessage.toString();
    }

    public TimeRange getExpectedWorkTime(String empCode, LocalDate workDate) {
        TimeRange plannedCommuteTime = attMapper.getPlannedCommuteTime2(workDate, empCode);

        if (plannedCommuteTime == null) {
            return null;
        }

        LocalDateTime finalStart = plannedCommuteTime.getStartDateTime();
        LocalDateTime finalEnd = plannedCommuteTime.getEndDateTime();

        List<String> reqTypes = List.of("휴일", "연장", "조출", "전반차", "후반차", "조퇴");

        for (String type : reqTypes) {
            TimeRange requestWorkTime = attMapper.getRequestWorkTime2(workDate, empCode, type);
            if (requestWorkTime != null) {
                switch (type) {
                    case "휴일":
                        // 휴일이면 출근/퇴근 둘 다 바꾸기
                        finalStart = requestWorkTime.getStartDateTime();
                        finalEnd = requestWorkTime.getEndDateTime();
                        break;

                    case "연장":
                        // 연장은 퇴근시간만 바꾸기
                        finalEnd = requestWorkTime.getEndDateTime();
                        break;

                    case "조출":
                        // 조출은 출근시간만 바꾸기
                        finalStart = requestWorkTime.getStartDateTime();
                        break;

                    case "전반차":
                        // 전반차는 퇴근시간만 바꾸기
                        finalEnd = requestWorkTime.getEndDateTime();
                        break;

                    case "후반차":
                        // 후반차는 출근시간만 바꾸기
                        finalStart = requestWorkTime.getStartDateTime();
                        break;

                    case "조퇴":
                        // 조퇴는 퇴근시간만 바꾸기
                        finalEnd = requestWorkTime.getEndDateTime();
                        break;

                    default:
                        break;
                }
            }
        }
        return getTimeRange(workDate, finalStart, finalEnd);
    }

    private TimeRange getTimeRange(LocalDate workDate, LocalDateTime finalStart, LocalDateTime finalEnd) {
        LocalTime startTime = finalStart.toLocalTime();
        boolean startNextDay = !finalStart.toLocalDate().isEqual(workDate);

        LocalTime endTime = finalEnd.toLocalTime();
        boolean endNextDay = !finalEnd.toLocalDate().isEqual(workDate);

        return new TimeRange(workDate, startTime, startNextDay, endTime, endNextDay);
    }

    public List<TimeRange> getOutTime(String empCode, LocalDate workDate) {
        // 휴게 시간 (1개)
        TimeRange restTime = attMapper.getPlannedRestTime2(workDate, empCode);
        // 외출 시간 (1개)
        TimeRange outTime = attMapper.getRequestWorkTime2(workDate, empCode, "외출");

        // 둘 다 없으면 null 반환
        if (restTime == null && outTime == null) {
            return Collections.emptyList();
        }

        // 하나만 있으면 그대로 반환
        if (restTime == null) return List.of(outTime);
        if (outTime == null) return List.of(restTime);

        if (restTime.overlaps(outTime)) {
            LocalDateTime mergedStart = restTime.getStartDateTime().isBefore(outTime.getStartDateTime())
                    ? restTime.getStartDateTime()
                    : outTime.getStartDateTime();

            LocalDateTime mergedEnd = restTime.getEndDateTime().isAfter(outTime.getEndDateTime())
                    ? restTime.getEndDateTime()
                    : outTime.getEndDateTime();

            // 병합된 구간 1개 반환
            return List.of(getTimeRange(workDate, mergedStart, mergedEnd));
        }

        // 겹치지 않으면 둘 다 반환
        return List.of(restTime, outTime);
    }

    //실적 반영
    public TimeRange getValidWorkRange(String empCode, LocalDate workDate) {
        // 예정 근무시간 (계획 + 신청 포함)
        TimeRange expectedWorkTime = getExpectedWorkTime(empCode, workDate);
        // 실제 근무 시간
        TimeRange realCommuteTime = attMapper.getRealCommuteTime2(workDate, empCode);

        if (expectedWorkTime == null || realCommuteTime == null) {
            return null; // 근무 데이터 없음
        }

        // 겹치는 구간 계산
        LocalDateTime start = realCommuteTime.getStartDateTime().isBefore(expectedWorkTime.getStartDateTime())
                ? expectedWorkTime.getStartDateTime()
                : realCommuteTime.getStartDateTime();

        LocalDateTime end = realCommuteTime.getEndDateTime().isAfter(expectedWorkTime.getEndDateTime())
                ? expectedWorkTime.getEndDateTime()
                : realCommuteTime.getEndDateTime();

        // 겹치는 구간이 실제 존재하지 않으면 null
        if (!end.isAfter(start)) {
            return null;
        }

        return getTimeRange(workDate, start, end);
    }


    public List<TimeRange> getFinalWorkRanges(String empCode, LocalDate workDate) {
        LocalDate today = LocalDate.now();
        TimeRange workRange;

        // 오늘 이후는 계획값 사용
        if (!workDate.isBefore(today)) {
            workRange = getExpectedWorkTime(empCode, workDate);
        }
        // 과거만 실적 기준
        else {
            workRange = getValidWorkRange(empCode, workDate);
        }

        if (workRange == null) return Collections.emptyList();

        List<TimeRange> outTimes = getOutTime(empCode, workDate);
        if (outTimes.isEmpty()) return List.of(workRange);

        // 근무 구간에서 휴게/외출 차감
        List<TimeRange> tempRanges = new ArrayList<>();
        tempRanges.add(workRange);

        for (TimeRange out : outTimes) {
            List<TimeRange> newRanges = new ArrayList<>();

            for (TimeRange work : tempRanges) {
                LocalDateTime wStart = work.getStartDateTime();
                LocalDateTime wEnd = work.getEndDateTime();
                LocalDateTime oStart = out.getStartDateTime();
                LocalDateTime oEnd = out.getEndDateTime();

                // 겹치지 않으면 그대로 유지
                if (wEnd.isBefore(oStart) || wStart.isAfter(oEnd)) {
                    newRanges.add(work);
                    continue;
                }

                // 앞부분 남는 경우
                if (wStart.isBefore(oStart)) {
                    newRanges.add(getTimeRange(workDate, wStart, oStart));
                }

                // 뒷부분 남는 경우
                if (wEnd.isAfter(oEnd)) {
                    newRanges.add(getTimeRange(workDate, oEnd, wEnd));
                }
            }

            tempRanges = newRanges;
        }

        return tempRanges;
    }

    public double getWeeklyWorkHours(String empCode, LocalDate anyDate) {
        LocalDate monday = anyDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate prevSunday = monday.minusDays(1); // 월요일 이전 일요일
        LocalDate sunday = monday.plusDays(6); // 이번 주 일요일

        double totalHours = 0.0;

        for (LocalDate date = prevSunday; !date.isAfter(sunday); date = date.plusDays(1)) {
            List<TimeRange> finalWorkRanges = getFinalWorkRanges(empCode, date);
            if (finalWorkRanges.isEmpty()) continue;

            for (TimeRange range : finalWorkRanges) {
                // 월요일 이전 일요일: 자정(월요일) 이후만
                if (date.equals(prevSunday)) {
                    LocalDateTime mondayMidnight = monday.atStartOfDay();
                    if (range.getEndDateTime().isAfter(mondayMidnight)) {
                        LocalDateTime start = range.getStartDateTime().isBefore(mondayMidnight)
                                ? mondayMidnight : range.getStartDateTime();
                        totalHours += Duration.between(start, range.getEndDateTime()).toMinutes() / 60.0;
                    }
                }
                // 이번 주 일요일: 자정(다음 주 월요일) 이전까지만
                else if (date.equals(sunday)) {
                    LocalDateTime nextMondayMidnight = sunday.plusDays(1).atStartOfDay();
                    if (range.getStartDateTime().isBefore(nextMondayMidnight)) {
                        LocalDateTime end = range.getEndDateTime().isAfter(nextMondayMidnight)
                                ? nextMondayMidnight : range.getEndDateTime();
                        totalHours += Duration.between(range.getStartDateTime(), end).toMinutes() / 60.0;
                    }
                }
                // 월~토: 전체 인정
                else {
                    totalHours += range.getDurationMinutes() / 60.0;
                }
            }
        }

        return totalHours;
    }

    public void cancelAttendance(List<BaseAttEmpDto> attList) {
        for (BaseAttEmpDto dto : attList) {
            attMapper.deleteApprovalLine(dto.getRequestId());
            attMapper.updateAttendanceStatus("SAVED", dto.getRequestId());
        }
    }
}

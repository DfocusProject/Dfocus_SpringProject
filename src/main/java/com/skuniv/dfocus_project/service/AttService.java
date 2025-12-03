package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.domain.Time.TimeRange;
import com.skuniv.dfocus_project.dto.*;
import com.skuniv.dfocus_project.mapper.AttMapper;
import com.skuniv.dfocus_project.mapper.DeptMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class AttService {

    private final AttendanceValidateService attendanceValidateService;
    private final AttMapper attMapper;
    private final DeptMapper deptMapper;
    private final EmpService empService;
    private final ApprovalService approvalService;
    public String getRealWorkRecord(String empCode, LocalDate workDate) {

        // 1. 실제 출근 기록 조회
        TimeRecordDto actualCommuteRecord = attMapper.getActualCommuteRecord(empCode, workDate);

        // 2. 출근기록이 없으면 무조건 결근
        if (actualCommuteRecord == null) {
            return "결근";
        }

        // 3. 출근기록이 있는 경우 → 휴일근로 8시간 이상 신청 여부 확인
        boolean hasHolidayWorkOver8h = attMapper.hasHolidayWorkOver8Hours(empCode, workDate);

        if (hasHolidayWorkOver8h) {
            return "휴일근로";
        }

        // 4. 둘 다 아니면 plannedShiftName 반환
        String plannedShift = attMapper.getPlannedShift(empCode, workDate);

        return attMapper.getShiftName(plannedShift);
    }

    @Transactional
    public String saveAttendance(LocalDate workDate, List<BaseAttEmpDto> attList, String loginEmpCode) {
        StringBuilder message = new StringBuilder();

        for (BaseAttEmpDto dto : attList) {
            String empCode = dto.getEmpCode();

            // 상신 체크
            if (attMapper.alreadyRequested(dto.getRequestId())) {
                message.append(empCode).append(": 이미 상신되어 수정 불가\n");
                continue;  // 이건 저장 안 함
            }
            if("조퇴".equals(dto.getAttType())) {
                dto.setEndNextDay(dto.getStartNextDay());
                dto.setEndTime(dto.getStartTime());
            }
            // 반차 시간 계산
            if ("반차".equals(dto.getAttType())) {
                calculateHalfDayTime(dto, workDate);
            }

            // 검증 (경고만)
            String validateError = attendanceValidateService.validate(
                    dto, workDate, getRealWorkRecord(empCode, workDate), getExpectedWorkTime(empCode, workDate), getWeeklyWorkHours(empCode, workDate)
            );
            if (validateError != null) {
                message.append(empCode).append(": ").append(validateError).append("\n");
            }

            // 저장 (에러 있어도 진행)
            try {
                boolean isUpdate = dto.getRequestId() != null;

                if (isUpdate) {
                    attMapper.updateAttendanceRequest(dto, loginEmpCode, workDate);
                    attMapper.updateAttendanceRequestGeneral(dto);
                } else {
                    dto.setStatus("SAVED");
                    attMapper.insertAttendanceRequest(dto, loginEmpCode, workDate);
                    attMapper.insertAttendanceRequestGeneral(dto);
                }
            } catch (Exception e) {
                message.append(empCode).append(": 저장 실패 - ").append(e.getMessage()).append("\n");
            }
        }

        return !message.isEmpty() ? message.toString() : "저장되었습니다";
    }

    private void calculateHalfDayTime(BaseAttEmpDto dto, LocalDate workDate) {
        TimeRange planTime = attMapper.getPlannedCommuteTime2(workDate, dto.getEmpCode());
        LocalDateTime start = planTime.getStartDateTime();
        LocalDateTime end = planTime.getEndDateTime();

        if ("morningOff".equals(dto.getHalfType())) {
            System.out.println("들어왔어?");
            // 오전반차 → 오후만 근무
            end = start.plusHours(4);
            dto.setHalfType("전반차");
            System.out.println("dto.getHalfType() = " + dto.getHalfType());
        } else if ("afternoonOff".equals(dto.getHalfType())) {
            // 오후반차 → 오전만 근무
            start = start.plusHours(4);
            dto.setHalfType("후반차");
        }

        dto.setStartTime(start.toLocalTime());
        dto.setEndTime(end.toLocalTime());
        dto.setStartNextDay(!start.toLocalDate().equals(workDate));
        dto.setEndNextDay(!end.toLocalDate().equals(workDate));
    }

    public TimeRange getPlannedCommuteTime(String empCode, LocalDate workDate) {
        return attMapper.getPlannedCommuteTime2(workDate, empCode);
    }

    public void deleteAttendance(List<BaseAttEmpDto> attList) {
        for (BaseAttEmpDto dto : attList) {
            if (dto.getRequestId() != null) {
                attMapper.deleteAttendanceRecord(dto);
            }
        }
    }

    @Transactional
    public String requestAttendance(LocalDate workDate, List<BaseAttEmpDto> attList, String loginEmpCode) {
        StringBuilder successMessages = new StringBuilder();
        StringBuilder errorMessages = new StringBuilder();

        // 1. 먼저 저장
        saveAttendance(workDate, attList, loginEmpCode);

        for (BaseAttEmpDto dto : attList) {
            String empCode = dto.getEmpCode();

            // 2. 검증 - 에러 있으면 상신 안 함
            String validateError = attendanceValidateService.validate(
                    dto, workDate, getRealWorkRecord(empCode, workDate),
                    getExpectedWorkTime(empCode, workDate), getWeeklyWorkHours(empCode, workDate));
            if (validateError != null) {
                errorMessages.append(empCode).append(": 상신 불가 - ").append(validateError).append("\n");
                continue;  // 상신 안 함
            }

            // 3. 이미 상신되었는지 체크
            Long requestId = attMapper.findAttendanceRequestId(empCode, dto.getAttType(), workDate);
            if (attMapper.existRequestRecord(requestId)) {
                errorMessages.append(empCode).append(": 이미 상신됨\n");
                continue;
            }
            attMapper.updateAttendanceStatus("REQUESTED", requestId);
            // 4. 결재선 생성
            try {
                DeptDto dept = deptMapper.getDeptByEmpCode(empCode);
                String leader = deptMapper.getLeaderByDeptCode(dept.getDeptCode());

                // 본인 결재선
                attMapper.insertApprovalRecord(empCode, empCode, requestId, 1, "REQUESTED", now());

                // 리더 결재선
                if (loginEmpCode.equals(leader)) {
                    // 본인이 리더면 자동 승인
                    attMapper.insertApprovalRecord(empCode, leader, requestId, 2, "APPROVED", now());
                    approvalService.approve(dto.getRequestId(), loginEmpCode);
                    attMapper.updateAttendanceStatus("APPROVED", requestId);
                } else {
                    // 리더 승인 대기
                    attMapper.insertApprovalRecord(empCode, leader, requestId, 2, "PENDING", null);
                    attMapper.updateAttendanceStatus("REQUESTED", requestId);
                }

                successMessages.append(empCode).append(": 상신 완료\n");

            } catch (Exception e) {
                errorMessages.append(empCode).append(": 상신 실패 - ").append(e.getMessage()).append("\n");
            }
        }

        // 5. 결과 메시지
        StringBuilder result = new StringBuilder();
        if (!successMessages.isEmpty()) {
            result.append("=== 상신 완료 ===\n").append(successMessages).append("\n");
        }
        if (!errorMessages.isEmpty()) {
            result.append("=== 상신 불가 ===\n").append(errorMessages);
        }

        return !result.isEmpty() ? result.toString() : "처리 완료";
    }

    public TimeRange getExpectedWorkTime(String empCode, LocalDate workDate) {
        // 1. 기본 계획 근무시간 조회
        TimeRange plannedCommuteTime = attMapper.getPlannedCommuteTime2(workDate, empCode);
        if (plannedCommuteTime == null) {
            return null;
        }
        LocalDateTime finalStart = plannedCommuteTime.getStartDateTime();
        LocalDateTime finalEnd = plannedCommuteTime.getEndDateTime();

        // 2. 해당 날짜 모든 근태 신청 조회
        List<Map<String, Object>> allRequests = attMapper.getAllRequestWorkTimeForDate(workDate, empCode);

        // 3. 요청 타입별 TimeRange 맵핑
        Map<String, TimeRange> requestMap = new HashMap<>();
        for (Map<String, Object> req : allRequests) {
            String type = (String) req.get("attendance_type");
            String halfType = (String) req.get("half_type"); // 전반차 / 후반차

            LocalTime startTime = ((java.sql.Time) req.get("startTime")).toLocalTime();
            Boolean startNextDay = (Boolean) req.get("startNextDay");
            LocalTime endTime = ((java.sql.Time) req.get("endTime")).toLocalTime();
            Boolean endNextDay = (Boolean) req.get("endNextDay");

            // key 결정: 반차는 half_type 사용, 나머지는 attendance_type
            String key = "반차".equals(type) ? halfType : type;

            requestMap.put(key, new TimeRange(workDate, startTime, startNextDay, endTime, endNextDay));
        }

        // 4. 요청 타입별로 계획 근무시간 반영
        List<String> reqTypes = List.of("휴일", "연장", "조출", "전반차", "후반차", "조퇴");
        for (String type : reqTypes) {
            TimeRange requestWorkTime = requestMap.get(type);
            if (requestWorkTime != null) {
                switch (type) {
                    case "휴일":
                        // 출근/퇴근 모두 변경
                        finalStart = requestWorkTime.getStartDateTime();
                        finalEnd = requestWorkTime.getEndDateTime();
                        break;
                    case "연장":
                        finalEnd = requestWorkTime.getEndDateTime();
                        break;
                    case "조출":
                        finalStart = requestWorkTime.getStartDateTime();
                        break;
                    case "조퇴":
                        LocalDateTime proposedEnd = requestWorkTime.getStartDateTime(); // 퇴근 조정 시간
                        // finalStart와 finalEnd 사이일 때만 finalEnd 조정
                        if (!proposedEnd.isBefore(finalStart) && proposedEnd.isBefore(finalEnd)) {
                            finalEnd = proposedEnd;
                        }
                        break;
                    case "전반차":
                        System.out.println("전반차 들어옴");
                        finalStart = requestWorkTime.getEndDateTime();// 출근만 조정
                        System.out.println("requestWorkTime.getStartDateTime() = " + requestWorkTime.getEndDateTime());
                        System.out.println("finalStart = " + finalStart);
                        break;
                    case "후반차":
                        finalEnd = requestWorkTime.getStartDateTime(); // 퇴근만 조정
                        break;
                }
            }
        }

        // 5. 최종 TimeRange 생성 후 반환
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

        return totalHours < 0 ? 0 : totalHours;
    }

    public void cancelAttendance(List<BaseAttEmpDto> attList) {
        for (BaseAttEmpDto dto : attList) {
            attMapper.deleteApprovalLine(dto.getRequestId());
            attMapper.updateAttendanceStatus("SAVED", dto.getRequestId());
        }
    }
    private String validateEtcAttendance(BaseAttEmpDto dto) {

        StringBuilder errors = new StringBuilder();

        LocalDate start = dto.getStartDate();
        LocalDate end = dto.getEndDate();

        // 2. 기존 신청 목록 조회
        List<ExistingEtcRequestDto> existingRequests =
                attMapper.findExistingEtcRequests(dto.getEmpCode(), start, end);

        // 3. 날짜별 체크
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {

            String originShiftCode = attMapper.getPlannedShift(dto.getEmpCode(), date);

            // 기존 근무 동일
            if (originShiftCode.equals(dto.getNewShiftType())) {
                errors.append(dto.getEmpCode())
                        .append(" 직원은 ").append(date).append(" 기존 근무와 동일합니다.\n");
            }
            if(dto.getPlanType().equals("연차") || dto.getPlanType().equals("반차")){
                errors.append(dto.getEmpCode())
                        .append(" 직원은 ").append(date).append(" 반차/연차 사용으로 기타 근태 신청이 불가합니다.\n");
            }
            // 기존 신청 중복
            for (ExistingEtcRequestDto existing : existingRequests) {
                if (!date.isBefore(existing.getStartDate()) && !date.isAfter(existing.getEndDate())) {
                    errors.append(dto.getEmpCode())
                            .append(" 직원은 ").append(date)
                            .append(" 이미 ").append(existing.getEtcType())
                            .append(" 신청이 되어 있습니다 (")
                            .append(existing.getStartDate()).append("~")
                            .append(existing.getEndDate()).append(")\n");
                }
            }

            // 연차 + 반차 중복
            if ("06".equals(dto.getNewShiftType())) {
                int halfDayExists =
                        attMapper.existsAttendanceRequest(dto.getEmpCode(), date, "반차");

                if (halfDayExists > 0) {
                    errors.append(dto.getEmpCode())
                            .append(" 직원은 ").append(date)
                            .append(" 이미 반차가 신청되어 있어 연차를 신청할 수 없습니다.\n");
                }
            }
        }

        return errors.toString();
    }

    @Transactional
    public String saveEtcAttendance(List<BaseAttEmpDto> attList,
                                    String loginEmpCode) {

        StringBuilder errorBuilder = new StringBuilder();

        for (BaseAttEmpDto dto : attList) {
            try {
                if (dto.getRequestId() != null && attMapper.alreadyRequested(dto.getRequestId())) {
                    errorBuilder.append("이미 상신되어 수정 불가합니다");
                    continue;
                }
                String validationMsg = validateEtcAttendance(dto);

                if (!validationMsg.isEmpty()) {
                    errorBuilder.append(validationMsg).append("\n");
                }

                // 저장은 검증 에러가 있어도 계속 진행
                LocalDate start = dto.getStartDate();

                dto.setPlanType(attMapper.getPlannedShift(dto.getEmpCode(), start));
                dto.setStatus("SAVED");
                dto.setAttType("06".equals(dto.getNewShiftType()) ? "연차" : "기타");

                if (dto.getRequestId() == null) {
                    attMapper.insertAttendanceRequest(dto, loginEmpCode, start);
                    attMapper.insertEtcAttendance(dto);
                } else {
                    attMapper.updateAttendanceRequest(dto, loginEmpCode, start);
                    attMapper.updateEtcAttendance(dto);
                }

            } catch (Exception e) {
                errorBuilder.append(dto.getEmpCode())
                        .append(" 직원 저장 중 오류: ")
                        .append(e.getMessage())
                        .append("\n");
            }
        }

        String errorMessage = errorBuilder.toString().trim();
        return errorMessage.isEmpty() ? "저장되었습니다" : errorMessage;
    }
    @Transactional
    public String requestEtcAttendance(LocalDate workDate, List<BaseAttEmpDto> attList, String empCode) {

        saveEtcAttendance(attList, empCode);

        StringBuilder errorMessages = new StringBuilder();

        for (BaseAttEmpDto dto : attList) {
            Long requestId = attMapper.findAttendanceRequestId(dto.getEmpCode(), dto.getAttType(), workDate);

            if(workDate.isBefore(LocalDate.now())){
                errorMessages.append("기타 근태 신청은 당일신청/1일이전신청만 가능합니다");
                continue;
            }
            if (requestId != null && attMapper.existRequestRecord(requestId)) {
                errorMessages.append("이미 상신되어 재상신이 불가합니다");
                continue;
            }
            String validateMsg = validateEtcAttendance(dto);

            if (!validateMsg.isEmpty()) {
                errorMessages.append(validateEtcAttendance(dto)).append("\n");
                continue;
            }

            DeptDto dept = deptMapper.getDeptByEmpCode(dto.getEmpCode());
            String leader = deptMapper.getLeaderByDeptCode(dept.getDeptCode());
            //상태 변경
            attMapper.updateAttendanceStatus("REQUESTED", dto.getRequestId());
            // 본인 결재선
            attMapper.insertApprovalRecord(dto.getEmpCode(), dto.getEmpCode(), dto.getRequestId(), 1, "REQUESTED", now());

            // 리더 결재선
            if (empCode.equals(leader)) {
                attMapper.insertApprovalRecord(dto.getEmpCode(), leader, dto.getRequestId(), 2, "APPROVED", now());
                approvalService.approve(dto.getRequestId(), empCode);
                attMapper.updateAttendanceStatus("APPROVED", dto.getRequestId());
            } else {
                attMapper.insertApprovalRecord(dto.getEmpCode(), leader, dto.getRequestId(), 2, "PENDING", null);
            }
        }
        return !errorMessages.isEmpty() ? errorMessages.toString() : "상신 완료";
    }

    public List<AttEmpViewDto> getAttEmpListWithHolidayCheck(String attType, LocalDate workDate, List<String> empCode, String deptName) {

        List<AttEmpViewDto> empList = empService.getAttEmpList(attType, workDate, empCode, deptName);
        Iterator<AttEmpViewDto> iterator = empList.iterator();

        while (iterator.hasNext()) {
            AttEmpViewDto emp = iterator.next();

            boolean isHoliday = attMapper.isHoliday(emp.getEmpCode(), workDate);
            boolean isExtendedOrEarly = "연장".equals(attType) || "조출".equals(attType);

            // -----------------------------------------
            // A. 날짜 기준 휴일 필터링
            // -----------------------------------------
            if (isHoliday) {
                if (!"휴일".equals(attType)) {
                    if (isExtendedOrEarly) {
                        boolean hasOver8 = attMapper.hasHolidayWorkOver8Hours(emp.getEmpCode(), workDate);
                        if (!hasOver8) {
                            iterator.remove();
                            continue;
                        }
                    } else {
                        iterator.remove();
                        continue;
                    }
                }
            } else {
                // 평일일 때 "휴일" 타입이면 제거
                if ("휴일".equals(attType)) {
                    iterator.remove();
                    continue;
                }
            }


            // -----------------------------------------
            // B. 실적 (출근기록 판단) 1회만
            // -----------------------------------------
            String realType = getRealWorkRecord(emp.getEmpCode(), workDate);
            emp.setRealWorkRecord(realType);

            // -----------------------------------------
            // C. planned 시간 세팅
            // -----------------------------------------
            if (emp.getRequestId() == null) {

                // (1) 기본 planned 근무시간
                TimeRange planTime = getPlannedCommuteTime(emp.getEmpCode(), workDate);
                if (planTime != null) {
                    emp.setPlannedStartTime(planTime.getStartTime());
                    emp.setPlannedEndTime(planTime.getEndTime());
                }

                // (2) 그날 휴일근로 8시간 이상 신청 → 신청시간 덮어쓰기
                boolean hasHolidayOver8 = attMapper.hasHolidayWorkOver8Hours(emp.getEmpCode(), workDate);
                if (hasHolidayOver8) {
                    TimeRange holidayTime = attMapper.getRequestWorkTime2(workDate, emp.getEmpCode(), "휴일");
                    if (holidayTime != null) {
                        emp.setPlannedStartTime(holidayTime.getStartTime());
                        emp.setPlannedEndTime(holidayTime.getEndTime());
                    }
                }
            }

            // -----------------------------------------
            // D. 예상 근무시간
            // -----------------------------------------
            emp.setExpectedWorkHours(getWeeklyWorkHours(emp.getEmpCode(), workDate));
        }

        return empList;
    }
    //휴일/연장 근로 시간 계산
    public double getAttWorkHours(LocalDate workDate, String empCode, String attType) {
        LocalDate startDate = workDate.with(DayOfWeek.MONDAY);
        LocalDate endDate = workDate.with(DayOfWeek.SUNDAY);

        List<TimeRange> attRequestList = attMapper.getAttRequestRecord(startDate, endDate, empCode, attType);
        List<TimeRange> commuteRecordList = attMapper.getActualCommuteRecordList(startDate, endDate, empCode);

        double totalHours = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            LocalDate currentDate = date; // 람다에서 사용할 final 변수

            TimeRange holiday = attRequestList.stream()
                    .filter(tr -> tr.getBaseDate().equals(currentDate))
                    .findFirst()
                    .orElse(null);

            TimeRange commute = commuteRecordList.stream()
                    .filter(tr -> tr.getBaseDate().equals(currentDate))
                    .findFirst()
                    .orElse(null);

            // 둘 다 존재하면 겹치는 시간 계산
            if (holiday != null && commute != null
                    && commute.getStartDateTime() != null
                    && commute.getEndDateTime() != null) {

                LocalDateTime holidayStart = holiday.getStartDateTime();
                LocalDateTime holidayEnd   = holiday.getEndDateTime();
                LocalDateTime commuteStart = commute.getStartDateTime();
                LocalDateTime commuteEnd   = commute.getEndDateTime();

                LocalDateTime maxStart = holidayStart.isAfter(commuteStart) ? holidayStart : commuteStart;
                LocalDateTime minEnd   = holidayEnd.isBefore(commuteEnd) ? holidayEnd : commuteEnd;

                if (!maxStart.isAfter(minEnd)) {
                    Duration duration = Duration.between(maxStart, minEnd);
                    totalHours += duration.toMinutes() / 60.0;
                }
            }
        }

        return totalHours;
    }

    //연장 근로 시간 계산
}
package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.domain.Time.TimeRange;
import com.skuniv.dfocus_project.dto.AttEmpViewDto;
import com.skuniv.dfocus_project.dto.BaseAttEmpDto;
import com.skuniv.dfocus_project.dto.TimeRecordDto;
import org.apache.ibatis.annotations.Param;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

public interface AttMapper {

//    // 근태 신청 시간
//    Map<String, String> getRequestHours(String empCode, LocalDate date, String type);
//
//    // 계획된 출퇴근 시간
//    Map<String, LocalTime> getCommuteHours(String empCode, LocalDate date);

    // 사번이랑 날짜 선택하면 -> 실제 출퇴근 기록 시간을 DTO로 반환
    TimeRecordDto getActualCommuteRecord(String empCode, LocalDate selectedDate);
    // 사번이랑 날짜 선택 -> 그 날 그 사원의 계획 근태코드
    String getPlannedShift(@Param("empCode") String empCode,
                           @Param("selectedDate") LocalDate selectedDate);
    // 출근 시간 기록
    void addRecordOnCommute(String empCode, LocalDate today, LocalTime now);
    // 퇴근 시간 기론
    void addRecordOffCommute(String empCode, LocalDate today, LocalTime now);
    // 근태코드로 계획된 출 퇴근 시간 반환
    Map<String, String> getPlannedCommuteTime(String shiftCode);
    // 근태코드로 근태 명 반환
    String getShiftName(String shiftCode);
    //일반 근태 신청 수정
    void updateAttendanceRequest(BaseAttEmpDto dto, String applicant, LocalDate selectedDate);
    //일반 근태 신청
    void insertAttendanceRequest(BaseAttEmpDto dto, String applicant, LocalDate selectedDate);
    //해당 사원의 해당 날짜에 해당 구분 신청 이력이 존재하는 지 확인
    int existsAttendanceRequest(String empCode, LocalDate selectedDate, String attType);

    //근태 신청 기록 삭제
    void deleteAttendanceRecord(BaseAttEmpDto dto);

    void insertApprovalRecord(String emp_request, String emp_response, Long requestId, int sequenceNo, String status, LocalDateTime approval_time);

    Long findAttendanceRequestId(String empNo, String workType, LocalDate workDate);

    void updateAttendanceStatus(String status, Long requestId);

    boolean existRequestRecord(Long requestId);

    void updateAttendanceRequestGeneral(BaseAttEmpDto dto);

    void insertAttendanceRequestGeneral(BaseAttEmpDto dto);

    TimeRange getPlannedCommuteTime2(LocalDate workDate, String empCode);
    TimeRange getRealCommuteTime2(LocalDate workDate, String empCode);
    TimeRange getRequestWorkTime2(LocalDate workDate, String empCode, String reqType);

    TimeRange getPlannedRestTime2(LocalDate workDate, String empCode);

    void deleteApprovalLine(Long requestId);
}

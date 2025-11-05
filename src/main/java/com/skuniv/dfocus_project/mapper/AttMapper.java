package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.dto.AttEmpDto;
import com.skuniv.dfocus_project.dto.AttendanceDto;
import com.skuniv.dfocus_project.dto.CommuteRecordDto;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public interface AttMapper {

//    // 근태 신청 시간
//    Map<String, String> getRequestHours(String empCode, LocalDate date, String type);
//
//    // 계획된 출퇴근 시간
//    Map<String, LocalTime> getCommuteHours(String empCode, LocalDate date);

    // 사번이랑 날짜 선택하면 -> 실제 출퇴근 기록 시간을 DTO로 반환
    CommuteRecordDto getActualCommuteRecord(String empCode, LocalDate selectedDate);
    // 사번이랑 날짜 선택 -> 그 날 그 사원의 계획 근태코드
    String getPlannedShift(String empCode, LocalDate selectedDate);
    // 출근 시간 기록
    void addRecordOnCommute(String empCode, LocalDate today, LocalTime now);
    // 퇴근 시간 기론
    void addRecordOffCommute(String empCode, LocalDate today, LocalTime now);
    // 근태코드로 계획된 출 퇴근 시간 반환
    Map<String, String> getPlannedCommuteTime(String shiftCode);
    // 근태코드로 근태 명 반환
    String getShiftName(String shiftCode);
    //일반 근태 신청 수정
    void updateAttendanceRequest(AttendanceDto dto);
    //일반 근태 신청
    void insertAttendanceRequest(AttendanceDto dto);
    //해당 사원의 해당 날짜에 해당 구분 신청 이력이 존재하는 지 확인
    int existsAttendanceRequest(String empCode, LocalDate selectedDate, String attType);
    //사번, 날짜, 신청 구분으로 반환 사원 리스트 찾기
    AttEmpDto findAttendanceRecord(String empCode, LocalDate selectedDate, String attType);
    //근태 신청 기록 삭제
    void deleteAttendanceRecord(AttendanceDto dto);
}

package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.dto.att.DeptAttSearchDto;
import com.skuniv.dfocus_project.dto.att.DeptAttSearchResultDto;
import com.skuniv.dfocus_project.dto.att.EtcSearchDto;
import com.skuniv.dfocus_project.mapper.DeptHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeptHistoryService {
    private final DeptHistoryMapper deptHistoryMapper;
    private final AttService attService;
    public List<String> getChildDeptList(String deptCode) {
        return deptHistoryMapper.findChildDepts(deptCode);
    }

    public List<DeptAttSearchResultDto> getEmpList(DeptAttSearchDto deptAttSearchDto) {
        //근무일 사번 근무계획 부서로 사원코드 가져오기
        List<DeptAttSearchResultDto> emps = deptHistoryMapper.findEmpsBySearch(deptAttSearchDto);
        return emps;
    }

    public void calculateDeptAttResult(List<DeptAttSearchResultDto> emps, LocalDate workDate) {
        for(DeptAttSearchResultDto emp : emps) {
            String empCode = emp.getEmpCode();
            emp.setRealType(attService.getRealWorkRecord(empCode, workDate));
            emp.setExpectedTime(attService.getWeeklyWorkHours(empCode, workDate));
            emp.setHolidayTime(attService.getAttWorkHours(workDate, empCode,"휴일"));
            emp.setOverTime(attService.getAttWorkHours(workDate, empCode,"연장"));
        }
    }
}

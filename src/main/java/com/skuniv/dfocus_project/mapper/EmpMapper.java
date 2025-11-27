package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.domain.emp.Emp;
import com.skuniv.dfocus_project.dto.AttEmpViewDto;
import com.skuniv.dfocus_project.dto.EmpDto;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface EmpMapper {
    Emp findByEmpCode(String empCode);

    List<Emp> findByDepartmentId(String deptCode);

    void deleteEmpFromDept(List<String> empCodes);

    void updateDeptCode(List<String> empCodes, String newDeptCode);

    List<EmpDto> findEmpNoDept();

    void assignDept(List<String> empCodes, String deptCode);

    void setPattern(String deptCode, String patternCode);

    List<String> selectEmpsByPattern(String patternName);

    void insertEmpPlan(LocalDate workDate, String empCode, String deptCode, String shiftCode, String patternName, String holidayYn);

    List<AttEmpViewDto> findAttEmpList(String attType, LocalDate workDate, List<String> empCode, String deptName);

    List<AttEmpViewDto> findEtcAttEmpList(String planType, LocalDate workDate, String searchEmpCode, String deptName);

    boolean existsEmpPlan(LocalDate workDate, String empCode);

    void updateEmpPlan(LocalDate workDate, String empCode, String deptCode, String shiftCode, String patternName, String holidayYn);

    String getEmpRole(String emp);

    void setPersonalPattern(String empCode, String patternCode);

    void updateEmpStatus(String empCode, String leaveType);
}

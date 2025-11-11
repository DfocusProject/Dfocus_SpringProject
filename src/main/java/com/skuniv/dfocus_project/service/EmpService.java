package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.dto.AttEmpViewDto;
import com.skuniv.dfocus_project.dto.EmpDto;
import com.skuniv.dfocus_project.mapper.DeptMapper;
import com.skuniv.dfocus_project.mapper.EmpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EmpService {
    private final EmpMapper empMapper;
    private final DeptMapper deptMapper;

    public void removeFromDept(List<String> empCodes) {
        empMapper.deleteEmpFromDept(empCodes);
    }

    public void setDeptLeader(String emp, String deptCode) {
        deptMapper.updateDeptLeader(emp, deptCode);
    }

    public void moveDept(List<String> empCodes, String newDeptCode) {
        empMapper.updateDeptCode(empCodes, newDeptCode);
    }
    public List<EmpDto> getEmpListNoDept() {
        return empMapper.findEmpNoDept();
    }

    public void assignEmployeesToDept(List<String> empCodes, String deptCode) {
        empMapper.assignDept(empCodes, deptCode);
    }

    public void setPattern(String deptCode, String patternCode) {
        empMapper.setPattern(deptCode, patternCode);
    }

    public List<AttEmpViewDto> getAttEmpList(String attType, LocalDate workDate, String empCode, String deptName) {
        return empMapper.findAttEmpList(attType, workDate, empCode, deptName);
    }
}

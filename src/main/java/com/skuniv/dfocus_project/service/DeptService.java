package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.domain.dept.Dept;
import com.skuniv.dfocus_project.domain.emp.Emp;
import com.skuniv.dfocus_project.dto.EmpDto;
import com.skuniv.dfocus_project.mapper.DeptMapper;
import com.skuniv.dfocus_project.mapper.EmpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DeptService {
    private final DeptMapper deptMapper;
    private final EmpMapper empMapper;

    public void setPattern(String deptCode, String patternCode) {
        deptMapper.savePattern(deptCode, patternCode);
    }

    public List<Dept> getAllDepartments() {
        return deptMapper.findAll();
    }

    public Dept getDepartmentTree() {
        List<Dept> allDepartments = getAllDepartments();

        Map<String, List<Emp>> empMap = new HashMap<>();
        for (Dept dept : allDepartments) {
            List<Emp> employees = empMapper.findByDepartmentId(dept.getDeptCode());
            empMap.put(dept.getDeptCode(), employees);
        }

        Map<String, Dept> deptMap = new HashMap<>();
        for (Dept dept : allDepartments) {
            dept.setChildren(new ArrayList<>());
            dept.setEmployees(empMap.getOrDefault(dept.getDeptCode(), new ArrayList<>()));
            deptMap.put(dept.getDeptCode(), dept);
        }

        Dept root = null;
        for (Dept dept : allDepartments) {
            if (dept.getParentDept() == null || dept.getParentDept().isEmpty()) {
                root = dept;
            } else {
                Dept parent = deptMap.get(dept.getParentDept());
                if (parent != null) {
                    parent.getChildren().add(dept);
                }
            }
        }
        return root;
    }

    public void createDepartment(Dept dept) {
        deptMapper.insertDept(dept);
    }

    public Dept findByDeptCode(String deptCode) {
        return deptMapper.findByDeptCode(deptCode);
    }
    public List<EmpDto> getEmpByDeptCode(String deptCode){
        return deptMapper.findAllEmpByDeptCode(deptCode);
    }

    public void updateUseYn(String deptCode, String useYn) {
        deptMapper.updateUseYn(deptCode, useYn);
    }

    public boolean isExistDept(String deptCode) {
        return deptMapper.isExistDept(deptCode);
    }
}

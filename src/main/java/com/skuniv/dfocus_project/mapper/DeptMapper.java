package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.domain.dept.Dept;
import com.skuniv.dfocus_project.domain.emp.Emp;
import com.skuniv.dfocus_project.dto.EmpDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeptMapper {
    Dept findByDeptCode(String DeptCode);
    List<Dept> findAll();
    void insertDept(Dept dept);
    List<EmpDto> findAllEmpByDeptCode(String DeptCode);
    void updateDeptLeader(String emp, String deptCode);
}

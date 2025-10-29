package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.domain.emp.Emp;
import com.skuniv.dfocus_project.dto.EmpDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EmpMapper {
    List<Emp> findByDepartmentId(String deptCode);

    void deleteEmpFromDept(List<String> empCodes);

    void updateDeptCode(List<String> empCodes, String newDeptCode);

    List<EmpDto> findEmpNoDept();

    void assignDept(List<String> empCodes, String deptCode);
}

package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.domain.dept.Dept;
import com.skuniv.dfocus_project.domain.emp.Emp;
import com.skuniv.dfocus_project.dto.DeptDto;
import com.skuniv.dfocus_project.dto.EmpDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeptMapper {
    //부서에 근태 패턴 추가
    void savePattern(String deptCode, String patternCode);
    //부서코드로 부서 찾기
    Dept findByDeptCode(String DeptCode);
    //모든 부서 찾기
    List<Dept> findAll();
    //부서 추가
    void insertDept(Dept dept);
    //부서의 모든 사원 찾기
    List<EmpDto> findAllEmpByDeptCode(String DeptCode);
    //리더 업데이트 하기
    void updateDeptLeader(String emp, String deptCode);
    //사원번호로 부서 찾기
    DeptDto getDeptByEmpCode(String empCode);
    //부서코드로 리더 찾기
    String getLeaderByDeptCode(String DeptCode);
}

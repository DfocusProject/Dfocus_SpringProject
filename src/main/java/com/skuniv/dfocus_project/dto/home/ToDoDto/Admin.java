package com.skuniv.dfocus_project.dto.home.ToDoDto;

import lombok.Data;

import java.util.List;

@Data
public class Admin {
    private int noDeptEmployeeCount;        // 부서 미배정 사원
    private int noPatternEmployeeCount;     // 근태패턴 미지정 사원
    private List<String> noLeaderDeptList;  // 리더 미배정 부서
}

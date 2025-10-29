package com.skuniv.dfocus_project.domain.dept;

import com.skuniv.dfocus_project.domain.emp.Emp;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

@Getter
@Setter
public class Dept {
    private String deptCode;
    private String deptName;
    private String parentDept;
    private String deptLeader;
    private String deptCategory;
    private String startDate;
    private String useYn;
    private String workPattern;

    //====DB 반영 X 트리구조 때문에 생성한 필드, 수정 가능성 있음!!====
    private List<Dept> children;
    private List<Emp> employees;

    public Dept(String deptCode, String deptName, String parentDept, String deptCategory, String startDate, String useYn) {
        this.deptCode = deptCode;
        this.deptName = deptName;
        this.parentDept = parentDept;
        this.deptCategory = deptCategory;
        this.startDate = startDate;
        this.useYn = useYn;
    }
}

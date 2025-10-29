package com.skuniv.dfocus_project.controller;

import com.skuniv.dfocus_project.domain.dept.Dept;
import com.skuniv.dfocus_project.dto.EmpDto;
import com.skuniv.dfocus_project.service.DeptService;
import com.skuniv.dfocus_project.service.EmpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/dept")
public class DeptController {
    private final DeptService deptService;
    private final EmpService empService;

    @GetMapping("/main")
    public String departmentTree(Model model) {
            Dept rootDepartment = deptService.getDepartmentTree();
            model.addAttribute("rootDepartment", rootDepartment);
            return "dept/main";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam("id") String deptCode, Model model) {
        List<Dept> allDepts = deptService.getAllDepartments();
        Dept dept = deptService.findByDeptCode(deptCode);
        List<EmpDto> emps = deptService.getEmpByDeptCode(deptCode);
        List<EmpDto> empsNoDept = empService.getEmpListNoDept();
        model.addAttribute("dept", dept);
        model.addAttribute("emps", emps);
        model.addAttribute("allDepts", allDepts);
        model.addAttribute("empsNoDept", empsNoDept);

        return "dept/detail";  // 상세 페이지 뷰 이름
    }

    @GetMapping("/create")
        public String create(Model model){
            List<Dept> depts = deptService.getAllDepartments();
            model.addAttribute("depts", depts);
            return "dept/create";
    }

    @PostMapping("/create")
    public String create(@RequestParam String deptCode,
                         @RequestParam String deptName,
                         @RequestParam String parentDeptCode,
                         @RequestParam String deptCategory,
                         @RequestParam String startDate,
                         @RequestParam String useYn,
                         Model model) {
        String formattedDate = startDate.replace("-", "");
        String formattedUseYn;
        if ("on".equals(useYn)) {
            formattedUseYn = "Y";
        } else {
            formattedUseYn = "N";
        }

        Dept dept = new Dept(deptCode, deptName, parentDeptCode, deptCategory, formattedDate, formattedUseYn);
        deptService.createDepartment(dept);
        return "redirect:/dept/main";
    }

    @PostMapping("/empAction")
    public String empAction(@RequestParam(required = false) String deptCode,
                            @RequestParam List<String> empCodes,
                            @RequestParam String action,
                            @RequestParam(required = false) String newDeptCode) {

        switch (action) {
            case "delete":
                empService.removeFromDept(empCodes);
                break;
            case "setLeader":
                empService.setDeptLeader(empCodes.get(0), deptCode);
                break;
            case "moveDept":
                empService.moveDept(empCodes, newDeptCode);
                break;
            case "assignDept":
                empService.assignEmployeesToDept(empCodes, deptCode);
                break;
        }
        return "redirect:/dept/detail?id=" + deptCode; // 다시 상세 페이지로
    }
}

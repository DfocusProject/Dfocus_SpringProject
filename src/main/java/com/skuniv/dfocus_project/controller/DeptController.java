package com.skuniv.dfocus_project.controller;

import com.skuniv.dfocus_project.domain.dept.Dept;
import com.skuniv.dfocus_project.dto.EmpDto;
import com.skuniv.dfocus_project.service.DeptService;
import com.skuniv.dfocus_project.service.EmpService;
import com.skuniv.dfocus_project.service.PatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/dept")
public class DeptController {
    private final DeptService deptService;
    private final EmpService empService;
    private final PatternService patternService;
    @PostMapping("/setPersonalPattern")
    public String setPersonalPattern(
            @RequestParam("empCodes") List<String> empCodes,
            @RequestParam("patternCode") String patternCode) {
        empService.setPesonalPattern(empCodes, patternCode);
        return "redirect:/dept/main";
    }

    @PostMapping("/patternSave")
    public String patternSave(@RequestParam String deptCode, @RequestParam String patternCode) {
        deptService.setPattern(deptCode, patternCode);
        empService.setPattern(deptCode, patternCode);
        return "redirect:/dept/main";
    }
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
        List<String> patternList =  patternService.getPatternNames();
        model.addAttribute("dept", dept);
        model.addAttribute("emps", emps);
        model.addAttribute("allDepts", allDepts);
        model.addAttribute("empsNoDept", empsNoDept);
        model.addAttribute("patternList", patternList);

        return "dept/detail";  // 상세 페이지 뷰 이름
    }

    @GetMapping("/create")
        public String create(Model model){
            List<Dept> depts = deptService.getAllDepartments();
            model.addAttribute("depts", depts);
            return "dept/create";
    }
    @PostMapping("/changeLeave")
    public String changeLeave(@RequestParam List<String> empCodes, String leaveType){
        empService.setEmpToOff(empCodes, leaveType);
        return "dept/main";
    }
    @PostMapping("/create")
    public String create(@RequestParam String deptCode,
                         @RequestParam String deptName,
                         @RequestParam String parentDeptCode,
                         @RequestParam String deptCategory,
                         @RequestParam String startDate,
                         @RequestParam String useYn
                         ) {
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
                            @RequestParam(required = false) String newDeptCode,
                            RedirectAttributes redirectAttrs) {
        String message = "";
        switch (action) {
            case "delete":
                message = empService.removeFromDept(empCodes);
                break;
            case "setLeader":
                message = empService.setDeptLeader(empCodes, deptCode);
                break;
            case "moveDept":
                message = empService.moveDept(empCodes, newDeptCode);
                break;
            case "assignDept":
                empService.assignEmployeesToDept(empCodes, deptCode);
                String currentPattern = deptService.findByDeptCode(deptCode).getWorkPattern();
                empService.setPattern(deptCode, currentPattern);
                break;
        }
        if(message != null && !message.isEmpty()){
            redirectAttrs.addFlashAttribute("message", message);
        }
        return "redirect:/dept/detail?id=" + deptCode; // 다시 상세 페이지로
    }
}

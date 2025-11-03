package com.skuniv.dfocus_project.controller;

import com.skuniv.dfocus_project.domain.account.Account;
import com.skuniv.dfocus_project.dto.AttEmpDto;
import com.skuniv.dfocus_project.dto.DeptDto;
import com.skuniv.dfocus_project.service.DeptService;
import com.skuniv.dfocus_project.service.EmpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/att")
public class AttController {

    private final DeptService deptService;
    private final EmpService empService;

    @GetMapping("/general")
    public String getUserInfo(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // 세션에서 로그인한 계정 가져오기
        Account loginAccount = (Account) session.getAttribute("loginAccount");

        if (loginAccount != null) {
            String empCode = loginAccount.getEmpCode();

            // 부서 정보 조회
            DeptDto deptDto = deptService.getDeptByEmpCode(empCode); // 사번으로 부서 조회

            if (deptDto == null) {
                redirectAttributes.addFlashAttribute("error", "부서 정보가 없어 근태 신청이 불가합니다.");
                return "redirect:/home";
            }
            String deptName = deptDto.getDeptName();
            // 모델에 담기
            model.addAttribute("empCode", empCode);
            model.addAttribute("deptName", deptName);
            model.addAttribute("role", loginAccount.getRole().name()); // enum이면 .name()으로 String
        } else {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/"; // 로그인 필요 시 홈으로 리다이렉트
        }
        return "att/general";
    }
    @GetMapping("/search")
    public String search(@RequestParam String attType,
                         @RequestParam String workDate,
                         @RequestParam(required = false) String empCode,
                         @RequestParam String deptName,
                         Model model) {
        //해당 부서 해당 날짜에 근무계획에서 holiday_yn이 n인 사원
        List<AttEmpDto> empList = empService.getAttEmpList(attType, workDate, empCode, deptName);
        model.addAttribute("empList", empList);
        return "/att/general";
    }

}

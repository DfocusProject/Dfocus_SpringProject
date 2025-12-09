package com.skuniv.dfocus_project.controller.common;

import com.skuniv.dfocus_project.CustomUserDetails;
import com.skuniv.dfocus_project.dto.EmpDto;
import com.skuniv.dfocus_project.dto.home.AnnualLeaveDto;
import com.skuniv.dfocus_project.dto.home.CommuteDto;
import com.skuniv.dfocus_project.dto.home.ToDoDto.Admin;
import com.skuniv.dfocus_project.dto.home.ToDoDto.Leader;
import com.skuniv.dfocus_project.dto.home.ToDoDto.User;
import com.skuniv.dfocus_project.service.EmpService;
import com.skuniv.dfocus_project.service.HomeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;
    private final EmpService empService;
    @GetMapping("/home")
    public String HomeController(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        CommuteDto commuteDto = homeService.getLatestCommuteRecord(userDetails.getUsername());
        AnnualLeaveDto annualLeaveDto = homeService.getAnnualLeaveRecord(userDetails.getUsername());
        List<Double> weeklyWorkedHours = homeService.getWeeklyWorkedHours(userDetails.getUsername());
        EmpDto empInfo = empService.getEmpInfo(userDetails.getUsername());
        User user = homeService.getUserToDoInfo(userDetails.getUsername());
        Admin admin = homeService.getAdminToDoInfo(userDetails.getUsername());
        Leader leader = homeService.getLeaderToDoInfo(userDetails.getUsername());
        String workHours = homeService.calculateWorkHours(commuteDto);
        model.addAttribute("commuteDto", commuteDto);
        model.addAttribute("annualLeaveDto", annualLeaveDto);
        model.addAttribute("weeklyWorkedHours", weeklyWorkedHours);
        model.addAttribute("empInfo", empInfo);
        model.addAttribute("user", user);
        model.addAttribute("admin", admin);
        model.addAttribute("leader", leader);
        model.addAttribute("workHours", workHours);
        return "home";
    }
    @PostMapping("/clearSearchSession")
    @ResponseBody
    public void clearSearchSession(HttpSession session) {
        session.removeAttribute("etcSearchDto");
        session.removeAttribute("deptAttSearchDto");
    }
}

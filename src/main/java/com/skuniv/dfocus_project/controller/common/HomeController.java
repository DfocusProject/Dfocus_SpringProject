package com.skuniv.dfocus_project.controller.common;

import com.skuniv.dfocus_project.CustomUserDetails;
import com.skuniv.dfocus_project.dto.home.AnnualLeaveDto;
import com.skuniv.dfocus_project.dto.home.CommuteDto;
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
    @GetMapping("/home")
    public String HomeController(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        CommuteDto commuteDto = homeService.getLatestCommuteRecord(userDetails.getUsername());
        AnnualLeaveDto annualLeaveDto = homeService.getAnnualLeaveRecord(userDetails.getUsername());
        List<Double> weeklyWorkedHours = homeService.getWeeklyWorkedHours(userDetails.getUsername());
        for(Double weeklyWorkedHour : weeklyWorkedHours){
            System.out.println("weeklyWorkedHour = " + weeklyWorkedHour);
        }
        model.addAttribute("commuteDto", commuteDto);
        model.addAttribute("annualLeaveDto", annualLeaveDto);
        model.addAttribute("weeklyWorkedHours", weeklyWorkedHours);
        return "home";
    }
    @PostMapping("/clearSearchSession")
    @ResponseBody
    public void clearSearchSession(HttpSession session) {
        session.removeAttribute("etcSearchDto");
        session.removeAttribute("deptAttSearchDto");
    }
}

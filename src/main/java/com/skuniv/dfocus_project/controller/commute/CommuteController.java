package com.skuniv.dfocus_project.controller.commute;

import com.skuniv.dfocus_project.CustomUserDetails;
import com.skuniv.dfocus_project.service.CommuteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
@RequestMapping("/commute")
public class CommuteController {
    private final CommuteService commuteService;

    @PostMapping("/on")
    public String workOn(@AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        String message = commuteService.setWorkOnTime(userDetails.getUsername());
        redirectAttributes.addFlashAttribute("message", message); // flash attribute 사용
        return "redirect:/home";
    }


    @PostMapping("/off") // type = "on" 또는 "off"
    public String workOff(@AuthenticationPrincipal CustomUserDetails userDetails) {
        commuteService.setWorkOffTime(userDetails.getUsername());
        return "redirect:/home";
    }
}

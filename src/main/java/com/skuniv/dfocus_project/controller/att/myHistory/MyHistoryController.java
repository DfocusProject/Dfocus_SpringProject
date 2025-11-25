package com.skuniv.dfocus_project.controller.att.myHistory;

import com.skuniv.dfocus_project.CustomUserDetails;
import com.skuniv.dfocus_project.dto.history.historyListDto;
import com.skuniv.dfocus_project.service.MyHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/myHistory")

public class MyHistoryController {
    private final MyHistoryService myHistoryService;
    @GetMapping("/main")
    public String main() {
        return "myHistory/main";
    }
    @GetMapping("/search")
    public String search(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        String loginEmpCode = userDetails.getUsername();
        List<historyListDto> lists = myHistoryService.getMyAttRequestList(loginEmpCode);
        model.addAttribute("lists", lists);
        return "myHistory/main";
    }
}

package com.skuniv.dfocus_project.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {
    @GetMapping("/home")
    public String HomeController() {
        return "home";
    }
    @PostMapping("/clearSearchSession")
    @ResponseBody
    public void clearSearchSession(HttpSession session) {
        session.removeAttribute("etcSearchDto");
        session.removeAttribute("deptAttSearchDto");
    }
}

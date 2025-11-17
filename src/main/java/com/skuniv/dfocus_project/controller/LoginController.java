package com.skuniv.dfocus_project.controller;

import com.skuniv.dfocus_project.domain.account.Account;
import com.skuniv.dfocus_project.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @GetMapping({"/", "/login"})
    public String loginForm() {
        return "login"; // login.html
    }

}

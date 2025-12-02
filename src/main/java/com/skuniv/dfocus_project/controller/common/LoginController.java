package com.skuniv.dfocus_project.controller.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @GetMapping({"/", "/login"})
    public String loginForm() {
        return "login"; // login.html
    }

}

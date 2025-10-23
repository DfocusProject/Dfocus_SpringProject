package com.skuniv.dfocus_project.controller;

import com.skuniv.dfocus_project.domain.account.Account;
import com.skuniv.dfocus_project.service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    private final AccountService accountService;

    public LoginController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login"; // login.html
    }

    @PostMapping("/login")
    public String login(@RequestParam String empCode,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        Account account = accountService.login(empCode, password);
        if (account != null) {
            session.setAttribute("loginAccount", account);
            return "redirect:/home";
        } else {
            model.addAttribute("error", "사번 또는 비밀번호가 일치하지 않습니다.");
            //여기 model에 들어있는 거 FE에서 받아서 화면에 출력해야 됨 !!
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

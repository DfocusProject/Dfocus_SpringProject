package com.skuniv.dfocus_project.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType("text/html; charset=UTF-8");

        // alert 후 이전 페이지로 이동
        response.getWriter().write(
                "<script>alert('권한이 필요한 페이지입니다.'); history.back();</script>"
        );
    }
}

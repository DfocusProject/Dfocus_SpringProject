package com.skuniv.dfocus_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 테스트 용, 실제 서비스 시에는 enable 권장
                .authorizeHttpRequests(auth -> auth
                        // 퍼블릭 접근
                        .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**").permitAll()

                        // ADMIN 전용
                        .requestMatchers("/dept/**", "/pattern/**").hasRole("ADMIN")

                        // 로그인한 모든 사용자 접근 가능
                        .requestMatchers("/att/**").authenticated()

                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}

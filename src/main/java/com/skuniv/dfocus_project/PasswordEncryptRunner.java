package com.skuniv.dfocus_project;

import com.skuniv.dfocus_project.domain.account.Account;
import com.skuniv.dfocus_project.mapper.AccountMapper;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
@AllArgsConstructor
@Component
public class PasswordEncryptRunner implements CommandLineRunner {

    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        List<Account> accounts = accountMapper.findAll();
        int count = 0;

        for (Account acc : accounts) {
            String current = acc.getPassword();
            // 이미 암호화되어 있지 않은 경우만 처리
            if (!current.startsWith("$2a$")) {
                acc.setPassword(passwordEncoder.encode(current));
                accountMapper.updatePassword(acc); // <- MyBatis 방식으로 업데이트
                count++;
            }
        }

        System.out.println(count + "개의 계정 비밀번호가 암호화되었습니다!");
    }
}

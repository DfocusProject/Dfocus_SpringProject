package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.domain.account.Account;
import com.skuniv.dfocus_project.mapper.AccountMapper;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountMapper accountMapper;

    public AccountService(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    public Account login(String empCode, String password) {
        Account account = accountMapper.findByEmpCode(empCode);
        if (account != null && account.getPassword().equals(password)) {
            return account;
        }
        return null;
    }
}

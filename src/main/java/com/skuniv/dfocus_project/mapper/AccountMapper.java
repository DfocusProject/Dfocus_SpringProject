package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.domain.account.Account;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccountMapper {
    Account findByEmpCode(String empCode);
    List<Account> findAll();

    void updatePassword(Account acc);
}

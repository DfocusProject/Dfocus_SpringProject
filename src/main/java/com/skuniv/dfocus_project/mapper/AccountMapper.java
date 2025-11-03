package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.domain.account.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper {
    Account findByEmpCode(String empCode);
}

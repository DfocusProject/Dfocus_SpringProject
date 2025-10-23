package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.domain.account.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {
    Account findByEmpCode(@Param("empCode") String empCode);
}

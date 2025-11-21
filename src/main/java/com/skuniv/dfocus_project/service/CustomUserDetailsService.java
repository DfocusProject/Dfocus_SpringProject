package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.CustomUserDetails;
import com.skuniv.dfocus_project.domain.account.Account;
import com.skuniv.dfocus_project.domain.emp.Emp;
import com.skuniv.dfocus_project.dto.DeptDto;
import com.skuniv.dfocus_project.mapper.AccountMapper;
import com.skuniv.dfocus_project.mapper.DeptMapper;
import com.skuniv.dfocus_project.mapper.EmpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountMapper accountMapper;
    private final EmpMapper empMapper;
    private final DeptMapper deptMapper;

    @Override
    public UserDetails loadUserByUsername(String empCode) throws UsernameNotFoundException {
        Account account = accountMapper.findByEmpCode(empCode);
        if (account == null) {
            throw new UsernameNotFoundException(empCode + " not found");
        }
        Emp employee = empMapper.findByEmpCode(empCode);
        DeptDto dept = deptMapper.getDeptByEmpCode(empCode);
        return new CustomUserDetails(
                account.getEmpCode(),
                account.getPassword(),
                employee.getEmpName(),
                employee.getDeptCode(),
                dept.getDeptName(),
                List.of(() -> "ROLE_" + account.getRole().name())
        );
    }
}
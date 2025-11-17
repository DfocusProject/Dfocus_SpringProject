package com.skuniv.dfocus_project;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final String empCode;
    private final String password;
    private final String empName;
    private final String deptCode;
    private final String deptName;
    private final Collection<? extends GrantedAuthority> authorities;

    public String getEmpName() { return empName; }
    public String getDeptCode() { return deptCode; }
    public String getDeptName() {return deptName; }
    public String getRole() {
        if (authorities == null || authorities.isEmpty()) {
            return null;
        }
        // ROLE_ 접두사 제거
        return authorities.iterator().next().getAuthority().replace("ROLE_", "");
    }


    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return empCode; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

package com.skuniv.dfocus_project.domain.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
    String empCode;
    String password;
    Role role;
}

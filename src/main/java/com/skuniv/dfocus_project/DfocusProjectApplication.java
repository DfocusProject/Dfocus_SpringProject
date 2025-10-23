package com.skuniv.dfocus_project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.skuniv.dfocus_project.mapper")

public class DfocusProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(DfocusProjectApplication.class, args);
    }

}

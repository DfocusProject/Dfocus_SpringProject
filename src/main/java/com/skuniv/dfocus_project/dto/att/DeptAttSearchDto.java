package com.skuniv.dfocus_project.dto.att;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class DeptAttSearchDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;
    private String empCode;
    private String planType;
    private String department;
}

package com.skuniv.dfocus_project.dto.att;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class EtcSearchDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;
    private String planType;
    private String empCode;
}

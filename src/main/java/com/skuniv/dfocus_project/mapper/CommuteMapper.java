package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.dto.commute.CommuteTimeDto;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalTime;

@Mapper
public interface CommuteMapper {
    void setWorkOnTime(String username, LocalDate today, LocalTime now);

    boolean isAlreadyCommute(String username, LocalDate today);

    CommuteTimeDto getLatestWorkOnTime(String username);

    void setWorkOffTime(CommuteTimeDto time);
}

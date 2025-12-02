package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.dto.home.AnnualLeaveDto;
import com.skuniv.dfocus_project.dto.home.CommuteDto;
import org.apache.ibatis.annotations.Mapper;

import java.time.Year;

@Mapper
public interface HomeMapper {
    CommuteDto getLatestCommuteRecord(String username);

    AnnualLeaveDto getAnnualLeaveRecord(String username, Year now);
}

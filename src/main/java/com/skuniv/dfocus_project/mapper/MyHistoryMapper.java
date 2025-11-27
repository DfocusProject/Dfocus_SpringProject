package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.dto.att.MyHistorySearchDto;
import com.skuniv.dfocus_project.dto.history.historyListDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MyHistoryMapper {
    List<historyListDto> findMyAttRequestList(
            @Param("loginEmpCode") String loginEmpCode,
            @Param("myHistorySearchDto") MyHistorySearchDto myHistorySearchDto
    );
}

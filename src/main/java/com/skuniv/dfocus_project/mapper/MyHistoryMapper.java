package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.dto.history.historyListDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyHistoryMapper {
    List<historyListDto> findMyAttRequestList(String loginEmpCode);
}

package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.dto.att.DeptAttSearchDto;
import com.skuniv.dfocus_project.dto.att.DeptAttSearchResultDto;
import com.skuniv.dfocus_project.dto.att.EtcSearchDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeptHistoryMapper {
    List<String> findChildDepts(String deptCode);

    List<DeptAttSearchResultDto> findEmpsBySearch(DeptAttSearchDto deptAttSearchDto);
}

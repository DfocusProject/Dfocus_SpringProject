package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.dto.att.DeptAttSearchResultDto;
import com.skuniv.dfocus_project.dto.att.EtcSearchDto;
import com.skuniv.dfocus_project.mapper.DeptHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeptHistoryService {
    private final DeptHistoryMapper deptHistoryMapper;
    public List<String> getChildDeptList(String deptCode) {
        return deptHistoryMapper.findChildDepts(deptCode);
    }

    public List<DeptAttSearchResultDto> getResultList(EtcSearchDto etcSearchDto) {
        //근무일 사번 근무계획 부서로 사원코드 가져오기
        List<String> empCodes = deptHistoryMapper.findEmpsBySearch(etcSearchDto);
        return null;
    }
}

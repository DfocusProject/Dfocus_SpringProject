package com.skuniv.dfocus_project.service;

import com.skuniv.dfocus_project.dto.att.MyHistorySearchDto;
import com.skuniv.dfocus_project.dto.history.historyListDto;
import com.skuniv.dfocus_project.mapper.MyHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyHistoryService {
    private final MyHistoryMapper myHistoryMapper;
    public List<historyListDto> getMyAttRequestList(String loginEmpCode, MyHistorySearchDto myHistorySearchDto) {
        return myHistoryMapper.findMyAttRequestList(loginEmpCode, myHistorySearchDto);
    }
}

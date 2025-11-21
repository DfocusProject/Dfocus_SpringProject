package com.skuniv.dfocus_project.mapper;

import com.skuniv.dfocus_project.dto.approval.ApprovalSearchDto;
import com.skuniv.dfocus_project.dto.approval.DocumentDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ApprovalMapper {
    //문서리스트 가져오기
    List<DocumentDto> getDocumentList(ApprovalSearchDto approvalSearchDto, String LoginEmpCode);
}

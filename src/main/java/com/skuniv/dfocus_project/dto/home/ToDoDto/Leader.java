package com.skuniv.dfocus_project.dto.home.ToDoDto;

import lombok.Data;

@Data
public class Leader {
    private int generalCount;   // 일반 근태 신청 수
    private int etcCount;       // 기타 근태 신청 수
    private int pendingApprovalCount; // 승인해야 할 문서 수
}

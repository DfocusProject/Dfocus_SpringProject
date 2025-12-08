package com.skuniv.dfocus_project.dto.home.ToDoDto;

import lombok.Data;

@Data
public class User {
    private int generalCount;   // 일반 근태 신청 수
    private int etcCount;       // 기타 근태 신청 수

    // 금일 결재된 신청서
    private int approvedCount;  // 승인된 신청 수
    private int rejectedCount;  // 반려된 신청 수
}

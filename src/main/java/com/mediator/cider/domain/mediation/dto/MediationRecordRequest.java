package com.mediator.cider.domain.mediation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MediationRecordRequest {
    private String content; // 사용자가 해당 라운드에 입력할 내용
}

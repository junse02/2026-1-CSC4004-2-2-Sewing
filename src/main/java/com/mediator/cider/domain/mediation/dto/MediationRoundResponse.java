package com.mediator.cider.domain.mediation.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 라운드 제출 결과 응답 DTO
 * 작성자: 황병부
 */
@Getter
@Builder
public class MediationRoundResponse {

    private String message;
    private String aiMessage;
    private boolean needsCycleExplore;
    private boolean riskFlag;
    private boolean completed;
}

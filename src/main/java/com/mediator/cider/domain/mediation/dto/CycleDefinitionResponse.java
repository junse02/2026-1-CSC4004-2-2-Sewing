package com.mediator.cider.domain.mediation.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 사이클 정의 결과 응답 DTO
 * 작성자: 황병부
 */
@Getter
@Builder
public class CycleDefinitionResponse {

    private Long sessionId;
    private String cycleDefinition;
}

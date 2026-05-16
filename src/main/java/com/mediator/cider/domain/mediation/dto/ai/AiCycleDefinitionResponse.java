package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 서버 POST /ai/cycle 응답 (사이클 정의)
 * 작성자: 황병부
 */
@Getter
@NoArgsConstructor
public class AiCycleDefinitionResponse {

    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("cycle_definition")
    private String cycleDefinition;
}

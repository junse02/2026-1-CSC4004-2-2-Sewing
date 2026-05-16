package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 서버 POST /ai/round-analyze 응답
 * 작성자: 황병부
 */
@Getter
@NoArgsConstructor
public class AiRoundAnalyzeResponse {

    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("f_message")
    private String fMessage;

    @JsonProperty("m_message")
    private String mMessage;

    @JsonProperty("needs_cycle_definition")
    private boolean needsCycleDefinition;

    @JsonProperty("risk_flag")
    private boolean riskFlag;
}

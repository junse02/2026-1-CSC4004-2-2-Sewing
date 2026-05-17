package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

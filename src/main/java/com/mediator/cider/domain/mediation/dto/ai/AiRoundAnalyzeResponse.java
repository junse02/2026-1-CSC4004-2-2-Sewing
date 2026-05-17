package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AiRoundAnalyzeResponse {
    private Long sessionId;
    private String fMessage;
    private String mMessage;
    private boolean needsCycleDefinition;
    private boolean riskFlag;
}

package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CycleExploreResponse {
    private Long sessionId;
    private String fQuestion;
    private String mQuestion;
}

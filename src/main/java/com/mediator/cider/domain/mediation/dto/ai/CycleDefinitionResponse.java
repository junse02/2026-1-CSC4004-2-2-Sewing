package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CycleDefinitionResponse {
    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("cycle_definition")
    private String cycleDefinition;
}

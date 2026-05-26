package com.mediator.cider.domain.mediation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CycleDefinitionGetResponse {
    private String cycleDefinition;
    private String fMessage;
    private String mMessage;
}

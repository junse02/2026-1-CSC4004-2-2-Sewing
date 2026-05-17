package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CycleRequest {
    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("f_explore_answer")
    private String fExploreAnswer;

    @JsonProperty("m_explore_answer")
    private String mExploreAnswer;
}

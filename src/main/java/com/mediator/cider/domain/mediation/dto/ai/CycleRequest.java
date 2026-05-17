package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CycleRequest {
    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("f_explore_answer")
    private String fExploreAnswer;

    @JsonProperty("m_explore_answer")
    private String mExploreAnswer;
}

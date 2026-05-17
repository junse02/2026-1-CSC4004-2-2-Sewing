package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CycleExploreResponse {
    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("f_question")
    private String fQuestion;

    @JsonProperty("m_question")
    private String mQuestion;
}

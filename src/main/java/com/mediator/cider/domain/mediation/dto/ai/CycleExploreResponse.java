package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CycleExploreResponse {

    @JsonProperty("session_id")
    private Long sessionId;

    @JsonAlias("f_question")
    private String fQuestion;

    @JsonAlias("m_question")
    private String mQuestion;
}

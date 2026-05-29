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

    // AI 서버가 "question" 이라는 공통 질문을 내려주도록 변경
    @JsonAlias("question")
    private String question;
}

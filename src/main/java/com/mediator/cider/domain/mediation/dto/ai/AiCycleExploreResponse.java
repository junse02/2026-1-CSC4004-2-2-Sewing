package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 서버 POST /ai/cycle 응답 (탐색 질문)
 * 작성자: 황병부
 */
@Getter
@NoArgsConstructor
public class AiCycleExploreResponse {

    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("f_question")
    private String fQuestion;

    @JsonProperty("m_question")
    private String mQuestion;
}

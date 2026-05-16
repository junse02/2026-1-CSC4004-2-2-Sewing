package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 서버 POST /ai/cycle 요청
 * 작성자: 황병부
 */
@Getter
@AllArgsConstructor
public class AiCycleRequest {

    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("f_explore_answer")
    private String fExploreAnswer;

    @JsonProperty("m_explore_answer")
    private String mExploreAnswer;
}

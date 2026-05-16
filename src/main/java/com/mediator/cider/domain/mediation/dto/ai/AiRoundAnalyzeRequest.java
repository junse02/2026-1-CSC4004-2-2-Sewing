package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 서버 POST /ai/round-analyze 요청
 * 작성자: 황병부
 */
@Getter
@AllArgsConstructor
public class AiRoundAnalyzeRequest {

    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("f_reply")
    private String fReply;

    @JsonProperty("m_reply")
    private String mReply;
}

package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 서버 POST /ai/report 요청
 * 작성자: 황병부
 */
@Getter
@AllArgsConstructor
public class AiReportRequest {

    @JsonProperty("session_id")
    private Long sessionId;
}

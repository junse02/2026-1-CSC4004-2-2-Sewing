package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiRoundAnalyzeRequest {
    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("f_reply")
    private String fReply;

    @JsonProperty("m_reply")
    private String mReply;
}

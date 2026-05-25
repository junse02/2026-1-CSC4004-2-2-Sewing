package com.mediator.cider.domain.mediation.dto.ai;

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

    // AI 서버로부터 읽을 때는 "f_question" 키를 사용 (역직렬화)
    // FE로 보낼 때는 "f_question"을 무시하고 자바 변수명을 사용하여 "fQuestion"으로 응답 (직렬화)
    @JsonProperty(value = "f_question", access = JsonProperty.Access.WRITE_ONLY)
    private String fQuestion;

    @JsonProperty(value = "m_question", access = JsonProperty.Access.WRITE_ONLY)
    private String mQuestion;
}

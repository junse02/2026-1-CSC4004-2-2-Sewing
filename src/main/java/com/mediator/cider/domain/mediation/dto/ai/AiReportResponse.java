package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 서버 POST /ai/report 응답
 * 작성자: 황병부
 */
@Getter
@NoArgsConstructor
public class AiReportResponse {

    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("f_report")
    private ReportSections fReport;

    @JsonProperty("m_report")
    private ReportSections mReport;

    @Getter
    @NoArgsConstructor
    public static class ReportSections {

        @JsonProperty("emotion_summary")
        private String emotionSummary;

        @JsonProperty("partner_understanding")
        private String partnerUnderstanding;

        @JsonProperty("mediation_plans")
        private String mediationPlans;

        @JsonProperty("recommended_dialogues")
        private String recommendedDialogues;
    }
}

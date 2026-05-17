package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportResponse {
    @JsonProperty("session_id")
    private Long sessionId;

    @JsonProperty("f_report")
    private ReportSections fReport;

    @JsonProperty("m_report")
    private ReportSections mReport;
}

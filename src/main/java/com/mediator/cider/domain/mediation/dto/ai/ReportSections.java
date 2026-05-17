package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportSections {
    @JsonProperty("emotion_summary")
    private String emotionSummary;

    @JsonProperty("partner_understanding")
    private String partnerUnderstanding;

    @JsonProperty("mediation_plans")
    private String mediationPlans;

    @JsonProperty("recommended_dialogues")
    private String recommendedDialogues;
}

package com.mediator.cider.domain.mediation.dto.ai;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReportSections {
    private String emotionSummary;
    private String partnerUnderstanding;
    private String mediationPlans;
    private String recommendedDialogues;
}

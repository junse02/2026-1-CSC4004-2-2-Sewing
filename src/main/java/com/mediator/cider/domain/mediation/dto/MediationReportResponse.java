package com.mediator.cider.domain.mediation.dto;

import com.mediator.cider.domain.mediation.entity.MediationReport;
import lombok.Builder;
import lombok.Getter;

/**
 * 보고서 조회 응답 DTO
 * 작성자: 황병부
 */
@Getter
@Builder
public class MediationReportResponse {

    private Long sessionId;
    private String emotionSummary;
    private String partnerUnderstanding;
    private String mediationPlans;
    private String recommendedDialogues;

    public static MediationReportResponse from(MediationReport report) {
        return MediationReportResponse.builder()
                .sessionId(report.getSession().getId())
                .emotionSummary(report.getEmotionSummary())
                .partnerUnderstanding(report.getPartnerUnderstanding())
                .mediationPlans(report.getMediationPlans())
                .recommendedDialogues(report.getRecommendedDialogues())
                .build();
    }
}

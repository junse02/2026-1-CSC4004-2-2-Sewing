package com.mediator.cider.domain.mediation.entity;

import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 최종 요약 보고서 엔티티
 */
@Entity
@Table(name = "mediation_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MediationReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private MediationSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "text", nullable = false)
    private String emotionSummary;

    @Column(columnDefinition = "text", nullable = false)
    private String partnerUnderstanding;

    @Column(columnDefinition = "text", nullable = false)
    private String mediationPlans;

    @Column(columnDefinition = "text", nullable = false)
    private String recommendedDialogues;
}

package com.mediator.cider.domain.mediation.entity;

import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 최종 보고서 엔티티 (4개 섹션 분리 저장)
 * 한 세션당 2개 row (여성 보고서 + 남성 보고서)
 * 작성자: 황병부
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
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private MediationSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String emotionSummary;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String partnerUnderstanding;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String mediationPlans;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String recommendedDialogues;
}

package com.mediator.cider.domain.mediation.entity;

import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "mediation_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MediationSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private User participant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediationStatus status;

    @Column(nullable = false)
    private int currentRound;

    // AI 서버 연동 추가 필드
    @Builder.Default
    private Integer eftStage = 1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private String stageRounds = "{\"1\":0,\"2\":0,\"3\":0}";

    @Builder.Default
    private Integer stageProgress = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String detectedSignals;

    @Column(columnDefinition = "text")
    @Builder.Default
    private String cycleDefinition = "";

    @Builder.Default
    private Integer cycleSkipUntil = 0;

    // 사이클 질문 캐싱용 필드
    @Column(columnDefinition = "TEXT")
    private String fCycleQuestion;

    @Column(columnDefinition = "TEXT")
    private String mCycleQuestion;

    public void joinParticipant(User participant) {
        if (this.participant != null) {
            throw new IllegalStateException("이미 2명이 모두 참여한 방입니다.");
        }
        this.participant = participant;
        this.status = MediationStatus.IN_PROGRESS;
        this.currentRound = 1;
    }

    public void advanceRound() {
        this.currentRound++;
    }

    public void completeMediation() {
        this.status = MediationStatus.COMPLETED;
    }

    public void cacheCycleQuestions(String fQuestion, String mQuestion) {
        this.fCycleQuestion = fQuestion;
        this.mCycleQuestion = mQuestion;
    }
}

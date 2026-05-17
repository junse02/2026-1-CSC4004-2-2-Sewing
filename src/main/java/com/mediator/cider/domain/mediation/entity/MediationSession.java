package com.mediator.cider.domain.mediation.entity;

import com.mediator.cider.domain.user.entity.User;
import com.mediator.cider.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 갈등 중재 방 엔티티
 */
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

    // 방을 만든 사람 (초대자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    // 방에 초대받은 사람 (참여자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private User participant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediationStatus status; // 대기중, 진행중, 완료

    @Column(nullable = false)
    private int currentRound; // 현재 진행 중인 라운드 번호

    // AI 서버 연동 추가 필드
    @Builder.Default
    private Integer eftStage = 1; // smallint DEFAULT 1

    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private String stageRounds = "{\"1\":0,\"2\":0,\"3\":0}"; // jsonb DEFAULT '{"1":0,"2":0,"3":0}'

    @Builder.Default
    private Integer stageProgress = 0; // int DEFAULT 0

    @Column(columnDefinition = "jsonb")
    private String detectedSignals; // jsonb

    @Column(columnDefinition = "text")
    @Builder.Default
    private String cycleDefinition = ""; // text DEFAULT ''

    @Builder.Default
    private Integer cycleSkipUntil = 0; // int DEFAULT 0

    // 참여자가 방에 들어왔을 때 상태 업데이트
    public void joinParticipant(User participant) {
        if (this.participant != null) {
            throw new IllegalStateException("이미 2명이 모두 참여한 방입니다.");
        }
        this.participant = participant;
        this.status = MediationStatus.IN_PROGRESS;
        this.currentRound = 1; // 1라운드부터 시작
    }

    // 다음 라운드로 이동
    public void advanceRound() {
        this.currentRound++;
    }

    // 중재 완료 처리
    public void completeMediation() {
        this.status = MediationStatus.COMPLETED;
    }
}

package com.mediator.cider.domain.mediation.dto;

import com.mediator.cider.domain.mediation.entity.MediationSession;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MediationSessionResponse {
    private Long sessionId;
    private String initiatorNickname; // 방장 닉네임
    private String participantNickname; // 참여자 닉네임 (대기 중이면 null)
    private String status; // WAITING, IN_PROGRESS, COMPLETED
    private int currentRound;
    private LocalDateTime updatedAt; // 마지막 활동(업데이트) 시간

    public static MediationSessionResponse from(MediationSession session) {
        return MediationSessionResponse.builder()
                .sessionId(session.getId())
                .initiatorNickname(session.getInitiator().getNickname())
                .participantNickname(session.getParticipant() != null ? session.getParticipant().getNickname() : null)
                .status(session.getStatus().name())
                .currentRound(session.getCurrentRound())
                .updatedAt(session.getUpdatedAt())
                .build();
    }
}

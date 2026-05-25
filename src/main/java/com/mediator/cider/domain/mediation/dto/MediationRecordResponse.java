package com.mediator.cider.domain.mediation.dto;

import com.mediator.cider.domain.mediation.entity.MediationRecord;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MediationRecordResponse {
    private Long recordId;
    private Long sessionId;
    private String email;
    private String nickname;
    private String gender;
    private int roundNumber;
    private String content;
    private String aiResponse;

    public static MediationRecordResponse from(MediationRecord record) {
        return MediationRecordResponse.builder()
                .recordId(record.getId())
                .sessionId(record.getSession().getId())
                .email(record.getUser().getEmail())
                .nickname(record.getUser().getNickname())
                .gender(record.getUser().getGender())
                .roundNumber(record.getRoundNumber())
                .content(record.getContent())
                .aiResponse(record.getAiResponse())
                .build();
    }
}

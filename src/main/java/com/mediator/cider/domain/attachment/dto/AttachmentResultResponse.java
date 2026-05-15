package com.mediator.cider.domain.attachment.dto;

import com.mediator.cider.domain.attachment.entity.AttachmentType;
import com.mediator.cider.domain.attachment.entity.UserAttachment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AttachmentResultResponse {
    private AttachmentType type;
    private String typeDescription;
    private double anxietyScore;
    private double avoidanceScore;

    public static AttachmentResultResponse from(UserAttachment attachment) {
        return AttachmentResultResponse.builder()
                .type(attachment.getType())
                .typeDescription(attachment.getType().getDescription())
                .anxietyScore(attachment.getAnxietyScore())
                .avoidanceScore(attachment.getAvoidanceScore())
                .build();
    }
}

package com.mediator.cider.domain.attachment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttachmentType {
    SECURE("안정형"),
    ANXIOUS("불안형"),
    AVOIDANT("거부회피형"),
    FEARFUL_AVOIDANT("공포회피형");

    private final String description;
}

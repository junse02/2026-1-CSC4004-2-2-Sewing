package com.mediator.cider.domain.attachment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AttachmentSurveyRequest {
    // 36문항에 대한 응답 (1점 ~ 7점)
    private List<Integer> answers;
}

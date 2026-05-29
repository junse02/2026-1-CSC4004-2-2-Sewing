package com.mediator.cider.domain.feedback.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FeedbackRequest {
    private int rating; // 1~5점
    private String comment;
}

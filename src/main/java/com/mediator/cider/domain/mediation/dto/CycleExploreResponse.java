package com.mediator.cider.domain.mediation.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 사이클 탐색 질문 응답 DTO
 * 작성자: 황병부
 */
@Getter
@Builder
public class CycleExploreResponse {

    private Long sessionId;
    private String question;
}

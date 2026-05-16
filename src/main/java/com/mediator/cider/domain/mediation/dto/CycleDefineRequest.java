package com.mediator.cider.domain.mediation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사이클 탐색 답변 요청 DTO
 * 작성자: 황병부
 */
@Getter
@NoArgsConstructor
public class CycleDefineRequest {

    private String answer;
}

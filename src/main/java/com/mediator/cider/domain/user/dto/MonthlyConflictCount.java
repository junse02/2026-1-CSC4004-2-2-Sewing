package com.mediator.cider.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MonthlyConflictCount {
    private String month; // 예: "2026-05"
    private long count;
}

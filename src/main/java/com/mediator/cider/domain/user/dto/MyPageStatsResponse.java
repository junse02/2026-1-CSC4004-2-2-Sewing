package com.mediator.cider.domain.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyPageStatsResponse {
    private long totalConflictCount;
    private double agreementRate;
    private List<MonthlyConflictCount> monthlyConflictCounts;
}

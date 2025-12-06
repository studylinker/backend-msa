package com.study.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatsSummaryResponse {
    private long totalUsers;
    private long activeStudies;
    private long newSignupsToday;
}

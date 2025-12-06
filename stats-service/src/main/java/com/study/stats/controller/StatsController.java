package com.study.stats.controller;

import com.study.stats.dto.ChartResponse;
import com.study.stats.dto.StatsSummaryResponse;
import com.study.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    // 1) 스터디 생성 수
    @GetMapping("/study-count")
    public ChartResponse getStudyCount() {
        return statsService.getStudyCount();
    }

    // 2) 카테고리 비율
    @GetMapping("/member-ratio")
    public ChartResponse getMemberRatio() {
        return statsService.getMemberRatio();
    }

    // 3) 출석률
    @GetMapping("/attendance")
    public ChartResponse getAttendanceStats() {
        return statsService.getAttendanceStats();
    }

    // 4) 운영 대시보드 요약 정보임.
    @GetMapping("/summary")
    public StatsSummaryResponse getSummary() {
        return statsService.getSummary();
    }

}

package com.study.stats.controller;

import com.study.common.security.JwtUserInfo;
import com.study.stats.dto.ChartResponse;
import com.study.stats.dto.StatsSummaryResponse;
import com.study.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * 운영 대시보드 통계 조회 컨트롤러
 * - ADMIN 권한이 있어야 접근 가능 (/api/stats/**)
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    // 1) 스터디 생성 수 (월별)
    @GetMapping("/study-count")
    public ResponseEntity<ChartResponse> getStudyCount(@AuthenticationPrincipal JwtUserInfo principal) {
        ChartResponse response = statsService.getStudyCount();
        return ResponseEntity.ok(response);
    }

    // 2) 카테고리 비율
    @GetMapping("/member-ratio")
    public ResponseEntity<ChartResponse> getMemberRatio(@AuthenticationPrincipal JwtUserInfo principal) {
        ChartResponse response = statsService.getMemberRatio();
        return ResponseEntity.ok(response);
    }

    // 3) 출석률
    @GetMapping("/attendance")
    public ResponseEntity<ChartResponse> getAttendanceStats(@AuthenticationPrincipal JwtUserInfo principal) {
        ChartResponse response = statsService.getAttendanceStats();
        return ResponseEntity.ok(response);
    }

    // 4) 운영 대시보드 요약 정보
    @GetMapping("/summary")
    public ResponseEntity<StatsSummaryResponse> getSummary(@AuthenticationPrincipal JwtUserInfo principal) {
        StatsSummaryResponse response = statsService.getSummary();
        return ResponseEntity.ok(response);
    }

    // 5) CSV 내보내기
    @GetMapping("/export")
    public ResponseEntity<Resource> exportStatsCsv(@AuthenticationPrincipal JwtUserInfo principal) {

        String csv = statsService.generateCsv();

        ByteArrayResource resource =
                new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=stats.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(resource.contentLength())
                .body(resource);
    }
}

// StatsService.java
package com.study.stats.service;

import com.study.stats.dto.ChartResponse;
import com.study.stats.dto.StatsSummaryResponse;
import com.study.stats.repository.AttendanceStatsRepository;
import com.study.stats.repository.MemberRatioRepository;
import com.study.stats.repository.StudyStatsRepository;
import com.study.stats.repository.SummaryStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StudyStatsRepository studyRepo;
    private final AttendanceStatsRepository attendanceRepo;
    private final SummaryStatsRepository summaryStatsRepository;
    private final MemberRatioRepository ratioRepo;

    // ===========================
    // 1) 스터디 개설 수 (월별)
    // ===========================
    public ChartResponse getStudyCount() {
        List<Object[]> rows = studyRepo.getMonthlyStudyCount();

        List<String> labels = rows.stream()
                .map(r -> (String) r[0])
                .toList();

        List<Long> data = rows.stream()
                .map(r -> ((Number) r[1]).longValue())
                .toList();

        return new ChartResponse(labels, data);
    }

    // ===========================
    // 2) 카테고리 비율 (DB 직접 조회)
    // ===========================
    public ChartResponse getMemberRatio() {

        List<Object[]> rows = ratioRepo.getCategoryRatio();

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        for (Object[] row : rows) {
            String category = (String) row[0];
            Long count = ((Number) row[1]).longValue();

            labels.add(category);
            data.add(count);
        }

        return new ChartResponse(labels, data);
    }

    // ===========================
    // 3) 출석 상태 비율
    // ===========================
    public ChartResponse getAttendanceStats() {

        List<Object[]> rows = attendanceRepo.getAttendanceRatio();

        List<String> labels = rows.stream()
                .map(r -> (String) r[0])
                .toList();

        List<Long> data = rows.stream()
                .map(r -> ((Number) r[1]).longValue())
                .toList();

        return new ChartResponse(labels, data);
    }

    // ===========================
    // 4) 운영 대시보드 요약
    // ===========================
    public StatsSummaryResponse getSummary() {

        long totalUsers = summaryStatsRepository.countTotalUsers();
        long activeStudies = summaryStatsRepository.countActiveStudies();
        long newSignupsToday = summaryStatsRepository.countNewUsersToday();

        return new StatsSummaryResponse(
                totalUsers,
                activeStudies,
                newSignupsToday
        );
    }

    public String generateCsv() {
        long totalUsers = summaryStatsRepository.countTotalUsers();
        long activeStudies = summaryStatsRepository.countActiveStudies();
        long newUsersToday = summaryStatsRepository.countNewUsersToday();

        return "항목,값\n" +
                "총 회원," + totalUsers + "\n" +
                "활성 스터디," + activeStudies + "\n" +
                "오늘 가입," + newUsersToday + "\n";
    }
}



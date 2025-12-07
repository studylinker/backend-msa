package com.study.stats.service;

import com.study.stats.dto.ChartResponse;
import com.study.stats.dto.StatsSummaryResponse;
import com.study.stats.repository.AttendanceStatsRepository;
import com.study.stats.repository.MemberRatioRepository;
import com.study.stats.repository.StudyStatsRepository;
import com.study.stats.repository.SummaryStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 운영 대시보드 통계 서비스
 * - Users / Study_groups / Attendance 테이블을 대상으로 통계를 조회
 * - 다른 서비스 모듈(User, StudyGroup 등)에 직접 의존하지 않음
 */
@Service
@RequiredArgsConstructor
public class StatsService {

    private final StudyStatsRepository studyRepo;
    private final MemberRatioRepository ratioRepo;
    private final AttendanceStatsRepository attendanceRepo;
    private final SummaryStatsRepository summaryStatsRepository;

    /**
     * 1) 스터디 개설 수 (월별)
     */
    public ChartResponse getStudyCount() {
        List<Object[]> rows = studyRepo.getMonthlyStudyCount();

        List<String> labels = rows.stream()
                .map(r -> (String) r[0])              // month (YYYY-MM)
                .toList();

        List<Long> data = rows.stream()
                .map(r -> ((Number) r[1]).longValue()) // count
                .toList();

        return new ChartResponse(labels, data);
    }

    /**
     * 2) 카테고리 비율
     */
    public ChartResponse getMemberRatio() {
        List<Object[]> rows = ratioRepo.getCategoryRatio();

        List<String> labels = rows.stream()
                .map(r -> (String) r[0])               // category (JSON string)
                .toList();

        List<Long> data = rows.stream()
                .map(r -> ((Number) r[1]).longValue()) // count
                .toList();

        return new ChartResponse(labels, data);
    }

    /**
     * 3) 출석 상태 비율
     */
    public ChartResponse getAttendanceStats() {
        List<Object[]> rows = attendanceRepo.getAttendanceRatio();

        List<String> labels = rows.stream()
                .map(r -> (String) r[0])               // status: PRESENT / ABSENT / LATE
                .toList();

        List<Long> data = rows.stream()
                .map(r -> ((Number) r[1]).longValue()) // count
                .toList();

        return new ChartResponse(labels, data);
    }

    /**
     * 4) 요약 통계 (대시보드 상단)
     */
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
}
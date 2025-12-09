package com.study.stats.service;

import com.study.stats.dto.ChartResponse;
import com.study.stats.dto.StatsSummaryResponse;
import com.study.stats.dto.UserStatDTO;
import com.study.stats.repository.AttendanceStatsRepository;
import com.study.stats.repository.StudyStatsRepository;
import com.study.stats.repository.SummaryStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StudyStatsRepository studyRepo;
    private final AttendanceStatsRepository attendanceRepo;
    private final SummaryStatsRepository summaryStatsRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 1) 스터디 개설 수 (월별)
     */
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

    /**
     * 2) 카테고리 비율 (user-service로부터 categories 가져와 직접 집계)
     */
    public ChartResponse getMemberRatio() {

        String url = "http://user-service:10000/internal/users/stats";

        ResponseEntity<UserStatDTO[]> response =
                restTemplate.getForEntity(url, UserStatDTO[].class);

        UserStatDTO[] users = response.getBody();
        if (users == null || users.length == 0) {
            return new ChartResponse(
                    List.of("데이터 없음"),
                    List.of(0L)
            );
        }

        Map<String, Integer> countMap = new HashMap<>();

        // 카테고리 합치기
        for (UserStatDTO user : users) {
            if (user.getCategories() == null) continue;

            for (String tag : user.getCategories()) {
                String key = tag.toLowerCase().trim();
                countMap.put(key, countMap.getOrDefault(key, 0) + 1);
            }
        }

        List<String> labels = new ArrayList<>(countMap.keySet());

        List<Long> data = labels.stream()
                .map(label -> countMap.get(label).longValue())
                .collect(Collectors.toList());


        return new ChartResponse(labels, data);
    }

    /**
     * 3) 출석 상태 비율
     */
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

    /**
     * 4) 운영 요약
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

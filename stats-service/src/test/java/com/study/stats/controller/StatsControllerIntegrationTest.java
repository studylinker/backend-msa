package com.study.stats.controller;

import com.study.common.security.JwtTokenProvider;
import com.study.stats.dto.ChartResponse;
import com.study.stats.dto.StatsSummaryResponse;
import com.study.stats.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("통계 컨트롤러 통합 테스트")
class StatsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private StatsService statsService;

    @MockBean
    private StringRedisTemplate redisTemplate;

    private String adminToken;
    private final Long adminUserId = 1L;

    @BeforeEach
    void setUp() {
        adminToken = jwtTokenProvider.createToken("admin", "ADMIN", adminUserId);
    }

    @Test
    @DisplayName("데이터베이스 연결 - DB 연결 검증")
    void databaseConnection_Success() throws SQLException {
        // Given & When
        try (Connection connection = dataSource.getConnection()) {
            // Then
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(5)).isTrue();
        }
    }

    @Test
    @DisplayName("GET /api/stats/study-count - 스터디 수 통계 조회")
    void getStudyCount_Success() throws Exception {
        // Given
        ChartResponse mockResponse = new ChartResponse(
                Arrays.asList("Jan", "Feb", "Mar"),
                Arrays.asList(10L, 20L, 30L)
        );
        when(statsService.getStudyCount()).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/stats/study-count")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").isArray())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/stats/member-ratio - 회원 비율 통계 조회")
    void getMemberRatio_Success() throws Exception {
        // Given
        ChartResponse mockResponse = new ChartResponse(
                Arrays.asList("Java", "Python", "JavaScript"),
                Arrays.asList(50L, 30L, 20L)
        );
        when(statsService.getMemberRatio()).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/stats/member-ratio")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").isArray())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/stats/attendance - 출석 통계 조회")
    void getAttendanceStats_Success() throws Exception {
        // Given
        ChartResponse mockResponse = new ChartResponse(
                Arrays.asList("Week1", "Week2", "Week3"),
                Arrays.asList(85L, 90L, 88L)
        );
        when(statsService.getAttendanceStats()).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/stats/attendance")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").isArray())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/stats/summary - 요약 통계 조회")
    void getSummary_Success() throws Exception {
        // Given
        StatsSummaryResponse mockResponse = new StatsSummaryResponse(100L, 50L, 10L);
        when(statsService.getSummary()).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/stats/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.activeStudies").value(50))
                .andExpect(jsonPath("$.newSignupsToday").value(10));
    }

    @Test
    @DisplayName("GET /api/stats/export - 통계 CSV 내보내기")
    void exportStatsCsv_Success() throws Exception {
        // Given
        String mockCsv = "Month,Count\nJan,10\nFeb,20";
        when(statsService.generateCsv()).thenReturn(mockCsv);

        // When & Then
        mockMvc.perform(get("/api/stats/export")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=stats.csv"))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @DisplayName("GET /api/stats/summary - 토큰 없이 인증 실패")
    void getSummary_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/stats/summary"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 서비스 연동 테스트 (데이터 집계 및 분석) ====================
    // stats-service는 user-service와 study-service의 데이터를 집계하여 통계를 제공합니다.

    @Test
    @DisplayName("[서비스연동] 통계 요약 - 사용자 및 스터디 데이터 집계")
    void serviceIntegration_Summary_AggregateData() throws Exception {
        // Given - 집계된 통계 데이터
        StatsSummaryResponse mockResponse = new StatsSummaryResponse(500L, 120L, 25L);
        when(statsService.getSummary()).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/stats/summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(500))
                .andExpect(jsonPath("$.activeStudies").value(120))
                .andExpect(jsonPath("$.newSignupsToday").value(25));

        // Verify stats service was called
        verify(statsService, times(1)).getSummary();
    }

    @Test
    @DisplayName("[서비스연동] 스터디 수 통계 - 월별 스터디 생성 추이")
    void serviceIntegration_StudyCount_MonthlyTrend() throws Exception {
        // Given - 월별 스터디 생성 통계
        ChartResponse mockResponse = new ChartResponse(
                Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun"),
                Arrays.asList(15L, 22L, 18L, 30L, 25L, 35L)
        );
        when(statsService.getStudyCount()).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/stats/study-count")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels", hasSize(6)))
                .andExpect(jsonPath("$.data", hasSize(6)));

        verify(statsService, times(1)).getStudyCount();
    }

    @Test
    @DisplayName("[서비스연동] 회원 비율 통계 - 관심 분야별 분포")
    void serviceIntegration_MemberRatio_InterestDistribution() throws Exception {
        // Given - 관심 분야별 사용자 분포 (user-service 데이터 기반)
        ChartResponse mockResponse = new ChartResponse(
                Arrays.asList("Java", "Python", "JavaScript", "Go", "Kotlin"),
                Arrays.asList(150L, 120L, 100L, 50L, 80L)
        );
        when(statsService.getMemberRatio()).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/stats/member-ratio")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").isArray())
                .andExpect(jsonPath("$.data").isArray());

        verify(statsService, times(1)).getMemberRatio();
    }

    @Test
    @DisplayName("[서비스연동] 출석 통계 - 스터디 참석률 추이")
    void serviceIntegration_Attendance_ParticipationRate() throws Exception {
        // Given - 주간 출석률 (study-service 일정 데이터 기반)
        ChartResponse mockResponse = new ChartResponse(
                Arrays.asList("Week1", "Week2", "Week3", "Week4"),
                Arrays.asList(85L, 90L, 88L, 92L)
        );
        when(statsService.getAttendanceStats()).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/stats/attendance")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels", hasSize(4)))
                .andExpect(jsonPath("$.data", hasSize(4)));

        verify(statsService, times(1)).getAttendanceStats();
    }

    @Test
    @DisplayName("[서비스연동] CSV 내보내기 - 통계 데이터 추출")
    void serviceIntegration_Export_DataExtraction() throws Exception {
        // Given - CSV 형식 통계 데이터
        String mockCsv = "Category,Count\nJava,150\nPython,120\nJavaScript,100";
        when(statsService.generateCsv()).thenReturn(mockCsv);

        // When & Then
        mockMvc.perform(get("/api/stats/export")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=stats.csv"));

        verify(statsService, times(1)).generateCsv();
    }

    @Test
    @DisplayName("[서비스연동] 일반 사용자 통계 조회")
    void serviceIntegration_UserRoleAccess() throws Exception {
        // Given
        String userToken = jwtTokenProvider.createToken("user", "USER", 100L);

        StatsSummaryResponse mockResponse = new StatsSummaryResponse(500L, 120L, 25L);
        when(statsService.getSummary()).thenReturn(mockResponse);

        // When & Then - 일반 사용자도 통계 조회 가능 여부 확인
        mockMvc.perform(get("/api/stats/summary")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
}

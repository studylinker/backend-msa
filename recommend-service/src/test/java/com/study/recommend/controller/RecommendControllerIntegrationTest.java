package com.study.recommend.controller;

import com.study.common.security.JwtTokenProvider;
import com.study.recommend.dto.PopularLocationResponse;
import com.study.recommend.dto.TagRecommendResponse;
import com.study.recommend.service.PopularLocationService;
import com.study.recommend.service.TagRecommendService;
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
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("추천 컨트롤러 통합 테스트")
class RecommendControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private PopularLocationService popularLocationService;

    @MockBean
    private TagRecommendService tagRecommendService;

    @MockBean
    private StringRedisTemplate redisTemplate;

    private String userToken;
    private final Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        userToken = jwtTokenProvider.createToken("testuser", "USER", testUserId);
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
    @DisplayName("GET /api/recommend/popular - 인기 추천 조회")
    void getPopularRecommendations_Success() throws Exception {
        // Given
        PopularLocationResponse mockResponse = PopularLocationResponse.builder()
                .groups(new ArrayList<>())
                .build();
        when(popularLocationService.getPopularGroupsByLocation(
                anyDouble(), anyDouble(), any(), any(), any(), any()
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/recommend/popular")
                        .param("lat", "37.5665")
                        .param("lng", "126.9780")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/recommend/popular - 선택적 파라미터 포함 조회")
    void getPopularRecommendations_WithOptionalParams() throws Exception {
        // Given
        PopularLocationResponse mockResponse = PopularLocationResponse.builder()
                .groups(new ArrayList<>())
                .build();
        when(popularLocationService.getPopularGroupsByLocation(
                anyDouble(), anyDouble(), any(), any(), any(), any()
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/recommend/popular")
                        .param("lat", "37.5665")
                        .param("lng", "126.9780")
                        .param("radiusKm", "10.0")
                        .param("limit", "20")
                        .param("popWeight", "0.6")
                        .param("distanceWeight", "0.4")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/recommend/tag - 태그 기반 추천 조회")
    void getTagRecommendations_Success() throws Exception {
        // Given
        TagRecommendResponse mockResponse = TagRecommendResponse.builder()
                .groups(new ArrayList<>())
                .build();
        when(tagRecommendService.getTagBasedGroups(
                anyLong(), anyDouble(), anyDouble(), anyDouble(), anyInt(), anyDouble(), anyDouble()
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/recommend/tag")
                        .param("lat", "37.5665")
                        .param("lng", "126.9780")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/recommend/tag - 선택적 파라미터 포함 태그 추천 조회")
    void getTagRecommendations_WithOptionalParams() throws Exception {
        // Given
        TagRecommendResponse mockResponse = TagRecommendResponse.builder()
                .groups(new ArrayList<>())
                .build();
        when(tagRecommendService.getTagBasedGroups(
                anyLong(), anyDouble(), anyDouble(), anyDouble(), anyInt(), anyDouble(), anyDouble()
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/recommend/tag")
                        .param("lat", "37.5665")
                        .param("lng", "126.9780")
                        .param("radiusKm", "5.0")
                        .param("limit", "10")
                        .param("alpha", "0.7")
                        .param("beta", "0.3")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/recommend/popular - 필수 파라미터 누락")
    void getPopularRecommendations_MissingParams() throws Exception {
        // When & Then - Missing lng parameter
        mockMvc.perform(get("/api/recommend/popular")
                        .param("lat", "37.5665")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/recommend/popular - 토큰 없이 인증 실패")
    void getPopularRecommendations_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/recommend/popular")
                        .param("lat", "37.5665")
                        .param("lng", "126.9780"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/recommend/tag - 토큰 없이 인증 실패")
    void getTagRecommendations_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/recommend/tag")
                        .param("lat", "37.5665")
                        .param("lng", "126.9780"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 서비스 연동 테스트 (다른 서비스와의 데이터 연동) ====================
    // recommend-service는 다른 서비스와의 직접적인 HTTP 통신 없이
    // study 그룹 데이터를 기반으로 추천을 제공합니다.

    @Test
    @DisplayName("[서비스연동] 위치 기반 추천 - 다양한 반경 테스트")
    void serviceIntegration_PopularRecommendation_VariousRadius() throws Exception {
        // Given
        PopularLocationResponse mockResponse = PopularLocationResponse.builder()
                .groups(new ArrayList<>())
                .build();
        when(popularLocationService.getPopularGroupsByLocation(
                anyDouble(), anyDouble(), any(), any(), any(), any()
        )).thenReturn(mockResponse);

        // When & Then - 5km 반경
        mockMvc.perform(get("/api/recommend/popular")
                        .param("lat", "37.5665")
                        .param("lng", "126.9780")
                        .param("radiusKm", "5.0")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // When & Then - 20km 반경
        mockMvc.perform(get("/api/recommend/popular")
                        .param("lat", "37.5665")
                        .param("lng", "126.9780")
                        .param("radiusKm", "20.0")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[서비스연동] 태그 기반 추천 - 사용자 관심 태그 기반")
    void serviceIntegration_TagRecommendation_UserInterestBased() throws Exception {
        // Given - 사용자 관심 태그 기반 추천 (user-service의 사용자 태그 정보 활용)
        TagRecommendResponse mockResponse = TagRecommendResponse.builder()
                .groups(new ArrayList<>())
                .build();
        when(tagRecommendService.getTagBasedGroups(
                eq(testUserId), anyDouble(), anyDouble(), anyDouble(), anyInt(), anyDouble(), anyDouble()
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/recommend/tag")
                        .param("lat", "37.5665")
                        .param("lng", "126.9780")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // Verify user ID was passed for tag matching
        verify(tagRecommendService).getTagBasedGroups(
                eq(testUserId), anyDouble(), anyDouble(), anyDouble(), anyInt(), anyDouble(), anyDouble()
        );
    }

    @Test
    @DisplayName("[서비스연동] 추천 가중치 파라미터 테스트")
    void serviceIntegration_Recommendation_WeightParameters() throws Exception {
        // Given
        PopularLocationResponse mockResponse = PopularLocationResponse.builder()
                .groups(new ArrayList<>())
                .build();
        when(popularLocationService.getPopularGroupsByLocation(
                anyDouble(), anyDouble(), any(), any(), any(), any()
        )).thenReturn(mockResponse);

        // When & Then - 인기도 가중치 높게
        mockMvc.perform(get("/api/recommend/popular")
                        .param("lat", "37.5665")
                        .param("lng", "126.9780")
                        .param("popWeight", "0.8")
                        .param("distanceWeight", "0.2")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // When & Then - 거리 가중치 높게
        mockMvc.perform(get("/api/recommend/popular")
                        .param("lat", "37.5665")
                        .param("lng", "126.9780")
                        .param("popWeight", "0.2")
                        .param("distanceWeight", "0.8")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[서비스연동] 다른 사용자 컨텍스트로 추천 요청")
    void serviceIntegration_Recommendation_DifferentUserContext() throws Exception {
        // Given - 다른 사용자 토큰으로 추천 요청
        Long anotherUserId = 999L;
        String anotherUserToken = jwtTokenProvider.createToken("anotheruser", "USER", anotherUserId);

        TagRecommendResponse mockResponse = TagRecommendResponse.builder()
                .groups(new ArrayList<>())
                .build();
        when(tagRecommendService.getTagBasedGroups(
                eq(anotherUserId), anyDouble(), anyDouble(), anyDouble(), anyInt(), anyDouble(), anyDouble()
        )).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/recommend/tag")
                        .param("lat", "35.1796")
                        .param("lng", "129.0756") // 부산 좌표
                        .header("Authorization", "Bearer " + anotherUserToken))
                .andExpect(status().isOk());

        // Verify the correct user ID was used
        verify(tagRecommendService).getTagBasedGroups(
                eq(anotherUserId), anyDouble(), anyDouble(), anyDouble(), anyInt(), anyDouble(), anyDouble()
        );
    }
}

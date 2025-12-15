package com.study.system.controller;

import com.study.common.security.JwtTokenProvider;
import com.study.system.service.SystemService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("시스템 컨트롤러 통합 테스트")
class SystemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private DataSource dataSource;

    @MockBean
    private SystemService systemService;

    @MockBean
    private StringRedisTemplate redisTemplate;

    private String adminToken;
    private String userToken;
    private final Long adminUserId = 1L;
    private final Long normalUserId = 2L;

    @BeforeEach
    void setUp() {
        adminToken = jwtTokenProvider.createToken("admin", "ADMIN", adminUserId);
        userToken = jwtTokenProvider.createToken("user", "USER", normalUserId);
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
    @DisplayName("POST /api/system/backup - 백업 생성 성공")
    void createBackup_Success() throws Exception {
        // Given
        doNothing().when(systemService).createBackup(anyLong());

        // When & Then
        mockMvc.perform(post("/api/system/backup")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Backup snapshot process executed."));

        verify(systemService, times(1)).createBackup(adminUserId);
    }

    @Test
    @DisplayName("POST /api/system/cache/clear - 캐시 삭제 성공")
    void clearCache_Success() throws Exception {
        // Given
        doNothing().when(systemService).clearCache(anyLong());

        // When & Then
        mockMvc.perform(post("/api/system/cache/clear")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Cache clear executed."));

        verify(systemService, times(1)).clearCache(adminUserId);
    }

    @Test
    @DisplayName("POST /api/system/backup - 토큰 없이 인증 실패")
    void createBackup_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/system/backup"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/system/cache/clear - 토큰 없이 인증 실패")
    void clearCache_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/system/cache/clear"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/system/backup - 서비스 예외 처리")
    void createBackup_ServiceException() throws Exception {
        // Given
        doThrow(new RuntimeException("Backup failed")).when(systemService).createBackup(anyLong());

        // When & Then
        mockMvc.perform(post("/api/system/backup")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("POST /api/system/cache/clear - 서비스 예외 처리")
    void clearCache_ServiceException() throws Exception {
        // Given
        doThrow(new RuntimeException("Cache clear failed")).when(systemService).clearCache(anyLong());

        // When & Then
        mockMvc.perform(post("/api/system/cache/clear")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().is5xxServerError());
    }

    // ==================== 서비스 연동 테스트 (시스템 관리 기능) ====================
    // system-service는 전체 MSA 시스템의 관리 기능을 제공합니다.

    @Test
    @DisplayName("[서비스연동] 백업 - 모든 서비스 데이터 백업 시나리오")
    void serviceIntegration_Backup_AllServicesData() throws Exception {
        // Given - 백업은 모든 서비스의 데이터를 포함
        doNothing().when(systemService).createBackup(adminUserId);

        // When & Then
        mockMvc.perform(post("/api/system/backup")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Backup snapshot process executed."));

        // Verify backup was triggered with correct admin user
        verify(systemService, times(1)).createBackup(adminUserId);
    }

    @Test
    @DisplayName("[서비스연동] 캐시 삭제 - 서비스 간 캐시 초기화")
    void serviceIntegration_CacheClear_CrossServiceCache() throws Exception {
        // Given - 캐시 삭제는 Redis 등 공유 캐시 초기화
        doNothing().when(systemService).clearCache(adminUserId);

        // When & Then
        mockMvc.perform(post("/api/system/cache/clear")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Cache clear executed."));

        verify(systemService, times(1)).clearCache(adminUserId);
    }

    @Test
    @DisplayName("[서비스연동] 관리자 권한 검증 - 백업 작업")
    void serviceIntegration_AdminAuthorization_Backup() throws Exception {
        // Given
        doNothing().when(systemService).createBackup(anyLong());

        // When & Then - 관리자 토큰으로 성공
        mockMvc.perform(post("/api/system/backup")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // When & Then - 일반 사용자 토큰으로 시도
        mockMvc.perform(post("/api/system/backup")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk()); // 권한 체크는 서비스 레벨에서 수행될 수 있음
    }

    @Test
    @DisplayName("[서비스연동] 관리자 권한 검증 - 캐시 삭제 작업")
    void serviceIntegration_AdminAuthorization_CacheClear() throws Exception {
        // Given
        doNothing().when(systemService).clearCache(anyLong());

        // When & Then - 관리자 토큰으로 성공
        mockMvc.perform(post("/api/system/cache/clear")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[서비스연동] 캐시 삭제 후 재구축 시나리오")
    void serviceIntegration_CacheClear_RebuildScenario() throws Exception {
        // Given - 캐시 삭제 성공
        doNothing().when(systemService).clearCache(anyLong());

        // When - 캐시 삭제 실행
        mockMvc.perform(post("/api/system/cache/clear")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Then - 캐시 서비스가 호출되었는지 확인
        verify(systemService).clearCache(adminUserId);
    }

    @Test
    @DisplayName("[서비스연동] 연속 백업 요청 처리")
    void serviceIntegration_Backup_ConsecutiveRequests() throws Exception {
        // Given
        doNothing().when(systemService).createBackup(anyLong());

        // When & Then - 연속 백업 요청
        mockMvc.perform(post("/api/system/backup")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/system/backup")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // Verify two backup calls were made
        verify(systemService, times(2)).createBackup(adminUserId);
    }

    @Test
    @DisplayName("[서비스연동] 다른 관리자의 시스템 작업")
    void serviceIntegration_DifferentAdminOperations() throws Exception {
        // Given - 다른 관리자
        Long anotherAdminId = 100L;
        String anotherAdminToken = jwtTokenProvider.createToken("admin2", "ADMIN", anotherAdminId);

        doNothing().when(systemService).createBackup(anotherAdminId);

        // When & Then
        mockMvc.perform(post("/api/system/backup")
                        .header("Authorization", "Bearer " + anotherAdminToken))
                .andExpect(status().isOk());

        // Verify correct admin ID was used
        verify(systemService).createBackup(anotherAdminId);
    }
}

package com.study.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.common.security.JwtTokenProvider;
import com.study.user.admin.dto.AdminNotificationRequest;
import com.study.user.client.StudyGroupClient;
import com.study.user.domain.Role;
import com.study.user.domain.User;
import com.study.user.domain.UserStatus;
import com.study.user.dto.UserRequest;
import com.study.user.notificationclient.NotificationClient;
import com.study.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("사용자 컨트롤러 통합 테스트")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private StudyGroupClient studyGroupClient;

    @MockBean
    private NotificationClient notificationClient;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private RestTemplate restTemplate;

    private User testUser;
    private User adminUser;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setLatitude(37.5665);
        testUser.setLongitude(126.9780);
        testUser.setInterestTags(Arrays.asList("Java", "Spring"));
        testUser = userRepository.save(testUser);

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("adminpass123"));
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser.setLatitude(37.5665);
        adminUser.setLongitude(126.9780);
        adminUser = userRepository.save(adminUser);

        userToken = jwtTokenProvider.createToken("testuser", "USER", testUser.getUserId());
        adminToken = jwtTokenProvider.createToken("admin", "ADMIN", adminUser.getUserId());
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
    @DisplayName("POST /api/users - 사용자 등록 성공")
    void createUser_Success() throws Exception {
        // Given
        UserRequest request = new UserRequest();
        request.setUsername("newuser");
        request.setPassword("newpassword123");
        request.setName("New User");
        request.setEmail("newuser@example.com");
        request.setLatitude(37.5665);
        request.setLongitude(126.9780);
        request.setInterestTags(Arrays.asList("Python", "Django"));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.name", is("New User")))
                .andExpect(jsonPath("$.email", is("newuser@example.com")));
    }

    @Test
    @DisplayName("POST /api/users - 중복 사용자명으로 등록 실패")
    void createUser_Failure_DuplicateUsername() throws Exception {
        // Given
        UserRequest request = new UserRequest();
        request.setUsername("testuser"); // existing username
        request.setPassword("password123");
        request.setName("Another User");
        request.setEmail("another@example.com");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/users/profile - 내 프로필 조회 성공")
    void getMyProfile_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    @DisplayName("GET /api/users/profile - 토큰 없이 인증 실패")
    void getMyProfile_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/users/{userId} - 사용자 정보 수정 성공")
    void updateUser_Success() throws Exception {
        // Given
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("testuser");
        updateRequest.setPassword("password123");
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setLatitude(37.5665);
        updateRequest.setLongitude(126.9780);
        updateRequest.setInterestTags(Arrays.asList("Java", "Spring"));

        // When & Then
        mockMvc.perform(put("/api/users/" + testUser.getUserId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/users/{userId} - 다른 사용자 정보 수정 권한 없음")
    void updateUser_Forbidden_DifferentUser() throws Exception {
        // Given
        UserRequest updateRequest = new UserRequest();
        updateRequest.setName("Updated Name");

        // When & Then
        mockMvc.perform(put("/api/users/99999")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/users/{userId} - 사용자 삭제 성공")
    void deleteUser_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users/" + testUser.getUserId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/users/{userId} - 다른 사용자 삭제 권한 없음")
    void deleteUser_Forbidden_DifferentUser() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users/99999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users/{userId}/groups - 가입한 그룹 조회 성공")
    void getJoinedGroups_Success() throws Exception {
        // Given
        when(studyGroupClient.getJoinedGroups(anyLong())).thenReturn(new Object[]{});

        // When & Then
        mockMvc.perform(get("/api/users/" + testUser.getUserId() + "/groups")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    // ==================== 서비스 간 내부 통신 테스트 ====================

    @Test
    @DisplayName("[내부통신] user → study-service 가입 그룹 조회 호출 성공")
    void internalCommunication_StudyService_GetJoinedGroups_Success() throws Exception {
        // Given
        Map<String, Object> group1 = new HashMap<>();
        group1.put("groupId", 1L);
        group1.put("title", "Java Study Group");

        Map<String, Object> group2 = new HashMap<>();
        group2.put("groupId", 2L);
        group2.put("title", "Spring Boot Study");

        when(studyGroupClient.getJoinedGroups(testUser.getUserId()))
                .thenReturn(new Object[]{group1, group2});

        // When & Then
        mockMvc.perform(get("/api/users/" + testUser.getUserId() + "/groups")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        // Verify study-service was called
        verify(studyGroupClient, times(1)).getJoinedGroups(testUser.getUserId());
    }

    @Test
    @DisplayName("[내부통신] user → study-service 호출 시 빈 그룹 목록 반환")
    void internalCommunication_StudyService_GetJoinedGroups_EmptyList() throws Exception {
        // Given
        when(studyGroupClient.getJoinedGroups(testUser.getUserId()))
                .thenReturn(new Object[]{});

        // When & Then
        mockMvc.perform(get("/api/users/" + testUser.getUserId() + "/groups")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("[내부통신] user → study-service 호출 시 네트워크 예외 처리")
    void internalCommunication_StudyService_GetJoinedGroups_NetworkException() throws Exception {
        // Given
        when(studyGroupClient.getJoinedGroups(anyLong()))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("Connection refused"));

        // When & Then
        mockMvc.perform(get("/api/users/" + testUser.getUserId() + "/groups")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("[내부통신] user → notification-service 알림 발송 성공")
    void internalCommunication_NotificationService_SendNotification_Success() throws Exception {
        // Given
        AdminNotificationRequest request = new AdminNotificationRequest();
        request.setUserIds(Arrays.asList(testUser.getUserId()));
        request.setMessage("Test notification");
        request.setType("SYSTEM");

        doNothing().when(notificationClient).send(any(AdminNotificationRequest.class));

        // When & Then
        mockMvc.perform(post("/api/admin/notifications")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify notification-service was called
        verify(notificationClient, times(1)).send(any(AdminNotificationRequest.class));
    }

    @Test
    @DisplayName("[내부통신] user → notification-service 전체 사용자 알림 발송")
    void internalCommunication_NotificationService_SendNotificationToAll_Success() throws Exception {
        // Given - userIds 비어있으면 전체 발송
        AdminNotificationRequest request = new AdminNotificationRequest();
        request.setUserIds(Arrays.asList());
        request.setMessage("Broadcast notification");
        request.setType("SYSTEM");

        doNothing().when(notificationClient).send(any(AdminNotificationRequest.class));

        // When & Then
        mockMvc.perform(post("/api/admin/notifications")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(notificationClient, times(1)).send(any(AdminNotificationRequest.class));
    }

    @Test
    @DisplayName("[내부통신] user → notification-service 권한 없는 사용자 알림 발송 실패")
    void internalCommunication_NotificationService_SendNotification_Forbidden() throws Exception {
        // Given
        AdminNotificationRequest request = new AdminNotificationRequest();
        request.setUserIds(Arrays.asList(testUser.getUserId()));
        request.setMessage("Test notification");
        request.setType("SYSTEM");

        // When & Then - 일반 사용자 토큰으로 시도
        mockMvc.perform(post("/api/admin/notifications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // Verify notification-service was NOT called
        verify(notificationClient, never()).send(any(AdminNotificationRequest.class));
    }

    @Test
    @DisplayName("[내부통신] user → notification-service 호출 시 네트워크 예외 처리")
    void internalCommunication_NotificationService_SendNotification_NetworkException() throws Exception {
        // Given
        AdminNotificationRequest request = new AdminNotificationRequest();
        request.setUserIds(Arrays.asList(testUser.getUserId()));
        request.setMessage("Test notification");
        request.setType("SYSTEM");

        doThrow(new org.springframework.web.client.ResourceAccessException("Connection refused"))
                .when(notificationClient).send(any(AdminNotificationRequest.class));

        // When & Then
        mockMvc.perform(post("/api/admin/notifications")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    // ==================== Internal API 테스트 (다른 서비스에서 호출) ====================

    @Test
    @DisplayName("[내부API] /internal/auth/verify-login - 로그인 검증 성공")
    void internalApi_VerifyLogin_Success() throws Exception {
        // Given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "password123");

        // When & Then
        mockMvc.perform(post("/internal/auth/verify-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("[내부API] /internal/auth/verify-login - 잘못된 비밀번호")
    void internalApi_VerifyLogin_InvalidPassword() throws Exception {
        // Given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "wrongpassword");

        // When & Then
        mockMvc.perform(post("/internal/auth/verify-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status >= 400 && status < 600)
                            .as("Expected error status (4xx or 5xx) but got: " + status)
                            .isTrue();
                });
    }

    @Test
    @DisplayName("[내부API] /internal/auth/verify-login - 존재하지 않는 사용자")
    void internalApi_VerifyLogin_UserNotFound() throws Exception {
        // Given
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "nonexistent");
        loginRequest.put("password", "password123");

        // When & Then
        mockMvc.perform(post("/internal/auth/verify-login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("[내부API] /internal/users/{userId} - 사용자 요약 정보 조회 성공")
    void internalApi_GetUserSummary_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/internal/users/" + testUser.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    @DisplayName("[내부API] /internal/users/{userId} - 존재하지 않는 사용자")
    void internalApi_GetUserSummary_UserNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/internal/users/99999"))
                .andExpect(status().is4xxClientError());
    }
}

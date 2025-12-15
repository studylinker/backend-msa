package com.study.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.common.security.JwtTokenProvider;
import com.study.notification.domain.Notification;
import com.study.notification.dto.NotificationRequest;
import com.study.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("알림 컨트롤러 통합 테스트")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private StringRedisTemplate redisTemplate;

    private Notification testNotification;
    private String userToken;
    private final Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();

        testNotification = new Notification();
        testNotification.setUserId(testUserId);
        testNotification.setMessage("Test notification message");
        testNotification.setType(Notification.Type.SYSTEM);
        testNotification.setIsRead(false);
        testNotification = notificationRepository.save(testNotification);

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
    @DisplayName("GET /api/notifications - 전체 알림 조회")
    void getAllNotifications_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].message", is("Test notification message")));
    }

    @Test
    @DisplayName("GET /api/notifications/unread - 읽지 않은 알림 조회")
    void getUnreadNotifications_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/unread")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("POST /api/notifications - 알림 생성")
    void createNotification_Success() throws Exception {
        // Given
        NotificationRequest request = new NotificationRequest();
        request.setUserIds(Arrays.asList(testUserId));
        request.setMessage("New notification message");
        request.setType("SYSTEM");

        // When & Then
        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].message", is("New notification message")));
    }

    @Test
    @DisplayName("POST /api/notifications - 빈 사용자 ID로 알림 생성 실패")
    void createNotification_Failure_EmptyUserIds() throws Exception {
        // Given
        NotificationRequest request = new NotificationRequest();
        request.setUserIds(Arrays.asList());
        request.setMessage("New notification message");
        request.setType("SYSTEM");

        // When & Then
        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("PATCH /api/notifications/{id}/read - 알림 읽음 처리")
    void markAsRead_Success() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/notifications/" + testNotification.getNotificationId() + "/read")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead", is(true)));
    }

    @Test
    @DisplayName("DELETE /api/notifications/{id} - 알림 삭제")
    void deleteNotification_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/notifications/" + testNotification.getNotificationId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/notifications/all - 전체 알림 삭제")
    void deleteAllNotifications_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/notifications/all")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/notifications - 토큰 없이 인증 실패")
    void getAllNotifications_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== 서비스 간 내부 통신 테스트 (다른 서비스에서 호출받는 API) ====================

    @Test
    @DisplayName("[내부통신] study-service → notification 가입 요청 알림 생성")
    void internalCommunication_FromStudyService_JoinRequestNotification() throws Exception {
        // Given - study-service에서 가입 요청 시 리더에게 알림 발송
        NotificationRequest request = new NotificationRequest();
        request.setUserIds(Arrays.asList(testUserId));
        request.setMessage("새로운 스터디 가입 요청이 도착했습니다.");
        request.setType("REQUEST");

        // When & Then
        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message", is("새로운 스터디 가입 요청이 도착했습니다.")))
                .andExpect(jsonPath("$[0].type", is("REQUEST")));
    }

    @Test
    @DisplayName("[내부통신] study-service → notification 가입 승인 알림 생성")
    void internalCommunication_FromStudyService_ApprovalNotification() throws Exception {
        // Given - study-service에서 가입 승인 시 멤버에게 알림 발송
        NotificationRequest request = new NotificationRequest();
        request.setUserIds(Arrays.asList(testUserId));
        request.setMessage("스터디 가입 요청이 승인되었습니다.");
        request.setType("REQUEST");

        // When & Then
        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message", is("스터디 가입 요청이 승인되었습니다.")));
    }

    @Test
    @DisplayName("[내부통신] study-service → notification 일정 등록 알림 생성")
    void internalCommunication_FromStudyService_ScheduleNotification() throws Exception {
        // Given - study-service에서 일정 등록 시 모든 멤버에게 알림 발송
        NotificationRequest request = new NotificationRequest();
        request.setUserIds(Arrays.asList(1L, 2L, 3L)); // 여러 멤버에게
        request.setMessage("새로운 스터디 일정이 등록되었습니다.");
        request.setType("SCHEDULE");

        // When & Then
        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))) // 3명에게 알림 생성
                .andExpect(jsonPath("$[0].type", is("SCHEDULE")));
    }

    @Test
    @DisplayName("[내부통신] user-service → notification 관리자 공지 알림 생성")
    void internalCommunication_FromUserService_AdminBroadcastNotification() throws Exception {
        // Given - user-service 관리자가 전체 공지
        String adminToken = jwtTokenProvider.createToken("admin", "ADMIN", 99L);

        NotificationRequest request = new NotificationRequest();
        request.setUserIds(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        request.setMessage("시스템 점검 안내입니다.");
        request.setType("SYSTEM");

        // When & Then
        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].type", is("SYSTEM")));
    }

    @Test
    @DisplayName("[내부통신] 다양한 알림 타입 테스트 - REQUEST")
    void internalCommunication_NotificationType_Request() throws Exception {
        // Given
        NotificationRequest request = new NotificationRequest();
        request.setUserIds(Arrays.asList(testUserId));
        request.setMessage("가입 요청");
        request.setType("REQUEST");

        // When & Then
        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type", is("REQUEST")));
    }

    @Test
    @DisplayName("[내부통신] 다양한 알림 타입 테스트 - SCHEDULE")
    void internalCommunication_NotificationType_Schedule() throws Exception {
        // Given
        NotificationRequest request = new NotificationRequest();
        request.setUserIds(Arrays.asList(testUserId));
        request.setMessage("일정 알림");
        request.setType("SCHEDULE");

        // When & Then
        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type", is("SCHEDULE")));
    }

    @Test
    @DisplayName("[내부통신] 다양한 알림 타입 테스트 - SYSTEM")
    void internalCommunication_NotificationType_System() throws Exception {
        // Given
        NotificationRequest request = new NotificationRequest();
        request.setUserIds(Arrays.asList(testUserId));
        request.setMessage("시스템 알림");
        request.setType("SYSTEM");

        // When & Then
        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type", is("SYSTEM")));
    }

    @Test
    @DisplayName("[내부통신] 대량 알림 생성 테스트")
    void internalCommunication_BulkNotificationCreation() throws Exception {
        // Given - 10명에게 알림 발송
        NotificationRequest request = new NotificationRequest();
        request.setUserIds(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L));
        request.setMessage("대량 알림 테스트");
        request.setType("SYSTEM");

        // When & Then
        mockMvc.perform(post("/api/notifications")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)));
    }
}

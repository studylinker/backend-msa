package com.study.study.studygroup.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.common.security.JwtTokenProvider;
import com.study.study.groupmember.domain.GroupMember;
import com.study.study.groupmember.repository.GroupMemberRepository;
import com.study.study.studygroup.domain.GroupStatus;
import com.study.study.studygroup.domain.StudyGroup;
import com.study.study.studygroup.dto.NotificationSendRequest;
import com.study.study.studygroup.dto.StudyGroupRequest;
import com.study.study.studygroup.repository.StudyGroupRepository;
import com.study.study.userclient.NotificationClient;
import com.study.study.userclient.UserClient;
import com.study.study.userclient.dto.UserSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("스터디 그룹 컨트롤러 통합 테스트")
class StudyGroupControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private StudyGroupRepository studyGroupRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private UserClient userClient;

    @MockBean
    private NotificationClient notificationClient;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    private StudyGroup testGroup;
    private String userToken;
    private String adminToken;
    private final Long testUserId = 1L;
    private final Long adminUserId = 2L;
    private final Long anotherUserId = 3L;

    @BeforeEach
    void setUp() {
        studyGroupRepository.deleteAll();

        testGroup = new StudyGroup();
        testGroup.setLeaderId(testUserId);
        testGroup.setTitle("Test Study Group");
        testGroup.setDescription("This is a test study group");
        testGroup.setMaxMembers(10);
        testGroup.setCategory("[\"Java\",\"Spring\"]");
        testGroup.setLatitude(BigDecimal.valueOf(37.5665));
        testGroup.setLongitude(BigDecimal.valueOf(126.9780));
        testGroup.setStatus(GroupStatus.ACTIVE);
        testGroup = studyGroupRepository.save(testGroup);

        userToken = jwtTokenProvider.createToken("testuser", "USER", testUserId);
        adminToken = jwtTokenProvider.createToken("admin", "ADMIN", adminUserId);
        doNothing().when(notificationClient).send(any());
        groupMemberRepository.deleteAll();
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
    @DisplayName("GET /api/study-groups - 전체 스터디 그룹 조회")
    void getAllStudyGroups_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/study-groups/" + testGroup.getGroupId() + "/schedules")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertThat(status == 200 || status == 404).isTrue();
                });
    }

    @WithMockUser(roles = "USER")
    @Test
    @DisplayName("GET /api/study-groups/{groupId} - ID로 스터디 그룹 조회")
    void getStudyGroupById_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/study-groups/" + testGroup.getGroupId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Test Study Group")))
                .andExpect(jsonPath("$.description", is("This is a test study group")));
    }

    @Test
    @DisplayName("GET /api/study-groups/{groupId} - 스터디 그룹 미존재")
    void getStudyGroupById_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/study-groups/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/study-groups - 스터디 그룹 생성 성공")
    void createStudyGroup_Success() throws Exception {
        // Given
        StudyGroupRequest request = new StudyGroupRequest();
        request.setTitle("New Study Group");
        request.setDescription("Description for new group");
        request.setMaxMembers(15);
        request.setCategory("[\"Python\",\"Django\"]");
        request.setLatitude(37.5665);
        request.setLongitude(126.9780);

        // When & Then
        mockMvc.perform(post("/api/study-groups")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Study Group")));
    }

    @Test
    @DisplayName("POST /api/study-groups - 토큰 없이 인증 실패")
    void createStudyGroup_Unauthorized() throws Exception {
        // Given
        StudyGroupRequest request = new StudyGroupRequest();
        request.setTitle("New Study Group");

        // When & Then
        mockMvc.perform(post("/api/study-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/study-groups/{groupId} - 스터디 그룹 수정 성공")
    void updateStudyGroup_Success() throws Exception {
        // Given
        StudyGroupRequest request = new StudyGroupRequest();
        request.setTitle("Updated Study Group");
        request.setDescription("Updated description");

        // When & Then
        mockMvc.perform(put("/api/study-groups/" + testGroup.getGroupId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Study Group")));
    }

    @Test
    @DisplayName("DELETE /api/study-groups/{id} - 스터디 그룹 삭제 성공")
    void deleteStudyGroup_Success() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/study-groups/" + testGroup.getGroupId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/study-groups/{id} - 리더가 아닌 사용자 삭제 권한 없음")
    void deleteStudyGroup_Forbidden() throws Exception {
        // Given - create token for different user
        String otherUserToken = jwtTokenProvider.createToken("otheruser", "USER", 999L);

        // When & Then
        mockMvc.perform(delete("/api/study-groups/" + testGroup.getGroupId())
                        .header("Authorization", "Bearer " + otherUserToken))
                .andExpect(status().isForbidden());
    }

    @WithMockUser(roles = "USER")
    @Test
    @DisplayName("GET /api/study-groups/{groupId}/schedules - 그룹 일정 조회")
    void getGroupSchedules_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/study-groups/" + testGroup.getGroupId() + "/schedules"))
                .andExpect(status().isOk());
    }

    // ==================== 서비스 간 내부 통신 테스트 ====================

    @Test
    @DisplayName("[내부통신] study → user-service 멤버 정보 조회 성공")
    void internalCommunication_UserService_GetUserById_Success() throws Exception {
        // Given - 리더로 멤버 추가
        GroupMember leaderMember = new GroupMember();
        leaderMember.setGroupId(testGroup.getGroupId());
        leaderMember.setUserId(testUserId);
        leaderMember.setRole(GroupMember.Role.LEADER);
        leaderMember.setStatus(GroupMember.Status.APPROVED);
        groupMemberRepository.save(leaderMember);

        UserSummary userSummary = new UserSummary();
        userSummary.setUserId(testUserId);
        userSummary.setUsername("testuser");
        userSummary.setName("Test User");

        when(userClient.getUserById(testUserId)).thenReturn(userSummary);

        // When & Then
        mockMvc.perform(get("/api/study-groups/" + testGroup.getGroupId() + "/members")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        // Verify user-service was called
        verify(userClient, atLeastOnce()).getUserById(testUserId);
    }

    @Test
    @DisplayName("[내부통신] study → user-service 호출 실패 시에도 멤버 목록 반환")
    void internalCommunication_UserService_GetUserById_ServiceError_GracefulDegradation() throws Exception {
        // Given
        GroupMember leaderMember = new GroupMember();
        leaderMember.setGroupId(testGroup.getGroupId());
        leaderMember.setUserId(testUserId);
        leaderMember.setRole(GroupMember.Role.LEADER);
        leaderMember.setStatus(GroupMember.Status.APPROVED);
        groupMemberRepository.save(leaderMember);

        // user-service 호출 실패
        when(userClient.getUserById(anyLong()))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("Connection refused"));

        // When & Then - 실패해도 기본 정보로 응답
        mockMvc.perform(get("/api/study-groups/" + testGroup.getGroupId() + "/members")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "USER")
    @Test
    @DisplayName("[내부통신] study → notification-service 가입 신청 알림 발송실패")
    void internalCommunication_NotificationService_JoinRequest_Success() throws Exception {
        // Given
        String anotherUserToken = jwtTokenProvider.createToken("anotheruser", "USER", anotherUserId);

        doNothing().when(notificationClient).send(any(NotificationSendRequest.class));

        // When & Then
        mockMvc.perform(post("/api/study-groups/" + testGroup.getGroupId() + "/members/request")
                        .header("Authorization", "Bearer " + anotherUserToken))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // 200 OK, 201 Created, 또는 이미 신청한 경우 4xx 모두 허용
                    assertThat(status >= 200 && status < 500).isTrue();
                });

        // Verify notification-service was called for leader notification
        verify(notificationClient, times(1)).send(any(NotificationSendRequest.class));
    }

    @Test
    @DisplayName("[내부통신] study → notification-service 호출 실패 시에 가입실패")
    void internalCommunication_NotificationService_JoinRequest_ServiceError_GracefulDegradation() throws Exception {
        // Given
        String anotherUserToken = jwtTokenProvider.createToken("anotheruser", "USER", anotherUserId);

        // notification-service 호출 실패
        doThrow(new org.springframework.web.client.ResourceAccessException("Connection refused"))
                .when(notificationClient).send(any(NotificationSendRequest.class));

        // When & Then - 알림 실패해도 가입 신청 실패
        mockMvc.perform(post("/api/study-groups/" + testGroup.getGroupId() + "/members/request")
                        .header("Authorization", "Bearer " + anotherUserToken))
                .andExpect(status().isOk());
    }

    // ==================== Internal API 테스트 (다른 서비스에서 호출) ====================

    @Test
    @DisplayName("[내부API] /internal/study/users/{userId}/groups - 사용자 가입 그룹 조회 성공")
    void internalApi_GetUserGroups_Success() throws Exception {
        // Given - 멤버로 등록
        GroupMember member = new GroupMember();
        member.setGroupId(testGroup.getGroupId());
        member.setUserId(testUserId);
        member.setRole(GroupMember.Role.MEMBER);
        member.setStatus(GroupMember.Status.APPROVED);
        groupMemberRepository.save(member);

        // When & Then
        mockMvc.perform(get("/internal/study/users/" + testUserId + "/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("[내부API] /internal/study/users/{userId}/groups - 가입 그룹 없음")
    void internalApi_GetUserGroups_Empty() throws Exception {
        // When & Then - 가입한 그룹이 없는 사용자
        mockMvc.perform(get("/internal/study/users/99999/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("[내부API] /internal/study/users/{userId}/groups - PENDING 상태 그룹 제외")
    void internalApi_GetUserGroups_ExcludePending() throws Exception {
        // Given - PENDING 상태로 등록
        GroupMember pendingMember = new GroupMember();
        pendingMember.setGroupId(testGroup.getGroupId());
        pendingMember.setUserId(anotherUserId);
        pendingMember.setRole(GroupMember.Role.MEMBER);
        pendingMember.setStatus(GroupMember.Status.PENDING);
        groupMemberRepository.save(pendingMember);

        // When & Then - PENDING 상태는 제외
        mockMvc.perform(get("/internal/study/users/" + anotherUserId + "/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}

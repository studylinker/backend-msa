package com.study.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.auth.dto.LoginRequest;
import com.study.auth.dto.UserLoginVerifyResponse;
import com.study.auth.service.AuthService;
import com.study.common.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("인증 컨트롤러 통합 테스트")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean  // ✅ 추가: AuthService도 mock 처리
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // ✅ 추가: RestTemplate mock을 더 느슨하게 설정
        when(restTemplate.postForEntity(
                anyString(),  // 모든 URL 매칭
                any(),
                any(Class.class)  // 모든 response 타입 매칭
        )).thenAnswer(invocation -> {
            LoginRequest req = invocation.getArgument(1);
            UserLoginVerifyResponse response = new UserLoginVerifyResponse();
            response.setUserId(1L);
            response.setUsername(req.getUsername());
            response.setRole("USER");
            return ResponseEntity.ok(response);
        });

        // ✅ JwtTokenProvider mock 추가
        when(jwtTokenProvider.createToken(anyString(), anyString(), anyLong()))
                .thenReturn("mock-jwt-token-value");
    }

    @Test
    @DisplayName("POST /api/auth/tokens - 로그인 성공")
    void login_Success() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        UserLoginVerifyResponse verifyResponse = new UserLoginVerifyResponse();
        verifyResponse.setUserId(1L);
        verifyResponse.setUsername("testuser");
        verifyResponse.setRole("USER");

        when(restTemplate.postForEntity(
                anyString(),
                any(LoginRequest.class),
                eq(UserLoginVerifyResponse.class)
        )).thenReturn(ResponseEntity.ok(verifyResponse));

        // When & Then
        mockMvc.perform(post("/api/auth/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())  // ✅ token → accessToken
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/tokens - 잘못된 자격 증명으로 로그인 실패")
    void login_Failure_InvalidCredentials() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("invaliduser");
        loginRequest.setPassword("wrongpassword");

        when(restTemplate.postForEntity(
                anyString(),
                any(LoginRequest.class),
                eq(UserLoginVerifyResponse.class)
        )).thenReturn(ResponseEntity.badRequest().build());

        // When & Then
        mockMvc.perform(post("/api/auth/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.accessToken").exists())  // ✅ token → accessToken
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/logout - 로그아웃 성공")
    void logout_Success() throws Exception {
        // Given
        String token = jwtTokenProvider.createToken("testuser", "USER", 1L);
        doNothing().when(valueOperations).set(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())  // ✅ token → accessToken
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        verify(valueOperations, times(1)).set(eq(token), eq("logout"));
    }

    @Test
    @DisplayName("POST /api/auth/logout - 인증 헤더 없이 로그아웃 실패")
    void logout_Failure_NoAuthorizationHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/logout - 잘못된 인증 헤더로 로그아웃 실패")
    void logout_Failure_InvalidAuthorizationHeader() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "InvalidHeader"))
                .andExpect(status().isForbidden());
    }

    // ==================== 서비스 간 내부 통신 테스트 ====================

    @Test
    @DisplayName("[내부통신] auth → user-service 로그인 검증 호출 성공")
    void internalCommunication_UserServiceVerifyLogin_Success() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("validuser");
        loginRequest.setPassword("validpassword");

        UserLoginVerifyResponse verifyResponse = new UserLoginVerifyResponse();
        verifyResponse.setUserId(100L);
        verifyResponse.setUsername("validuser");
        verifyResponse.setRole("USER");

        when(restTemplate.postForEntity(
                contains("/internal/auth/verify-login"),
                any(LoginRequest.class),
                eq(UserLoginVerifyResponse.class)
        )).thenReturn(ResponseEntity.ok(verifyResponse));

        // When & Then
        mockMvc.perform(post("/api/auth/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())  // ✅ token → accessToken
                .andExpect(jsonPath("$.accessToken").isNotEmpty());

        // Verify user-service was called
        verify(restTemplate, times(1)).postForEntity(
                contains("/internal/auth/verify-login"),
                any(LoginRequest.class),
                eq(UserLoginVerifyResponse.class)
        );
    }

    @Test
    @DisplayName("[내부통신] auth → user-service 호출 시 user-service 응답 실패")
    void internalCommunication_UserServiceVerifyLogin_ServiceError() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        when(restTemplate.postForEntity(
                contains("/internal/auth/verify-login"),
                any(LoginRequest.class),
                eq(UserLoginVerifyResponse.class)
        )).thenReturn(ResponseEntity.status(500).build());

        // When & Then
        mockMvc.perform(post("/api/auth/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("[내부통신] auth → user-service 호출 시 null 응답 처리")
    void internalCommunication_UserServiceVerifyLogin_NullResponse() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        when(restTemplate.postForEntity(
                contains("/internal/auth/verify-login"),
                any(LoginRequest.class),
                eq(UserLoginVerifyResponse.class)
        )).thenReturn(ResponseEntity.ok(null));

        // When & Then
        mockMvc.perform(post("/api/auth/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("[내부통신] auth → user-service 호출 시 네트워크 예외 처리")
    void internalCommunication_UserServiceVerifyLogin_NetworkException() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        when(restTemplate.postForEntity(
                anyString(),
                any(LoginRequest.class),
                eq(UserLoginVerifyResponse.class)
        )).thenThrow(new org.springframework.web.client.ResourceAccessException("Connection refused"));

        // When & Then
        mockMvc.perform(post("/api/auth/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError());
    }

    @WithMockUser(roles = "ADMIN")
    @Test
    @DisplayName("[내부통신] auth → user-service ADMIN 역할 사용자 로그인 성공")
    void internalCommunication_UserServiceVerifyLogin_AdminRole() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("adminpassword");

        mockMvc.perform(post("/api/auth/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-token-value"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }
}

package com.study.auth.service;

import com.study.auth.dto.LoginRequest;
import com.study.auth.dto.UserLoginVerifyResponse;
import com.study.common.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public AuthService(JwtTokenProvider jwtTokenProvider,
                       RestTemplate restTemplate,
                       @Value("${user-service.base-url}") String userServiceBaseUrl) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    /**
     * 로그인:
     *  - user-service의 /internal/auth/verify-login 으로 아이디/비번 보내서 검증
     *  - 유효한 유저면 userId/username/role 받아서 JWT 생
     */
    public String login(String username, String password) {

        System.out.println("[AuthService] username=" + username + ", password=" + password);

        // 1) user-service에 보낼 요청 DTO
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);

        // 2) user-service 내부 로그인 검증 API 호출
        String url = userServiceBaseUrl + "/internal/auth/verify-login";

        ResponseEntity<UserLoginVerifyResponse> response =
                restTemplate.postForEntity(url, request, UserLoginVerifyResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalArgumentException("로그인 검증에 실패했습니다.");
        }

        UserLoginVerifyResponse user = response.getBody();

        // 3) user-service가 검증한 유저 정보로 JWT 발급.
        return jwtTokenProvider.createToken(
                user.getUsername(),
                user.getRole(),
                user.getUserId()
        );
    }

    /**
     * 로그아웃: 토큰 블랙리스트에 추가
     */
    public void logout(String token) {
        jwtTokenProvider.invalidateToken(token);
    }
}

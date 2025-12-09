package com.study.user.notificationclient;

import com.study.user.admin.dto.AdminNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${notification-service.base-url}")
    private String notificationBaseUrl;

    public void send(AdminNotificationRequest request) {

        String url = notificationBaseUrl + "/api/notifications";

        // 🔥 현재 로그인한 사용자의 JWT 가져오기
        String jwt = extractJwtToken();

        // 🔥 헤더에 Authorization 추가
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwt);

        HttpEntity<AdminNotificationRequest> entity =
                new HttpEntity<>(request, headers);

        restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Void.class
        );
    }

    // ===========================================
    // JWT 추출 로직
    // (JwtAuthenticationFilter가 SecurityContext에 저장한 값 꺼냄)
    // ===========================================
    private String extractJwtToken() {
        try {
            Object details = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getCredentials();   // <-- JwtAuthenticationFilter에서 저장한 토큰

            if (details instanceof String token) {
                return token;
            }
        } catch (Exception e) {
            System.out.println("⚠ JWT 추출 실패: " + e.getMessage());
        }

        return ""; // fallback (하지만 없음)
    }
}

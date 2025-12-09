package com.study.user.notificationclient;

import com.study.common.security.JwtTokenProvider;
import com.study.user.admin.dto.AdminNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestTemplate restTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${notification-service.base-url}")
    private String notificationBaseUrl;

    // 🔥 DB 관리자 계정 기반 내부 ADMIN 토큰 발급
    private String createInternalAdminToken() {
        return jwtTokenProvider.createToken(
                "admin",   // username
                "ADMIN",   // role
                1L         // userId
        );
    }

    public void send(AdminNotificationRequest request) {

        String url = notificationBaseUrl + "/api/notifications";

        // 🔥 내부 호출용 ADMIN JWT 생성
        String internalJwt = createInternalAdminToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + internalJwt);

        HttpEntity<AdminNotificationRequest> entity =
                new HttpEntity<>(request, headers);

        restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Void.class
        );
    }
}

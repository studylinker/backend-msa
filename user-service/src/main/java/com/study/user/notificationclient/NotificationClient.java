package com.study.user.notificationclient;

import com.study.user.admin.dto.AdminNotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${notification-service.base-url}")
    private String notificationBaseUrl;   // 예: http://notification-service:10000

    public void send(AdminNotificationRequest request) {
        String url = notificationBaseUrl + "/api/notifications";

        restTemplate.postForEntity(url, request, Void.class);
    }
}

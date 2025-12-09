package com.study.study.userclient;

import com.study.study.studygroup.dto.NotificationSendRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NotificationClient {

    private final RestTemplate restTemplate;
    private final String notificationBaseUrl;

    public NotificationClient(RestTemplate restTemplate,
                              @Value("${notification-service.base-url}") String notificationBaseUrl) {
        this.restTemplate = restTemplate;
        this.notificationBaseUrl = notificationBaseUrl;
    }

    public void send(NotificationSendRequest request) {
        String url = notificationBaseUrl + "/api/notifications";
        restTemplate.postForObject(url, request, Void.class);
    }
}

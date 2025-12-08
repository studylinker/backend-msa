package com.study.study.userclient;

import com.study.study.userclient.dto.UserSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserClient {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public UserClient(RestTemplate restTemplate,
                      @Value("${user-service.base-url}") String userServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.userServiceBaseUrl = userServiceBaseUrl;
    }

    public UserSummary getUserById(Long userId) {
        String url = userServiceBaseUrl + "/internal/users/" + userId;
        return restTemplate.getForObject(url, UserSummary.class);
    }
}
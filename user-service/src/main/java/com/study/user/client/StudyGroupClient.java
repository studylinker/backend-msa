package com.study.user.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StudyGroupClient {

    private final RestTemplate restTemplate;
    private final String studyServiceBaseUrl;

    public StudyGroupClient(
            RestTemplate restTemplate,
            @Value("${study-service.base-url}") String studyServiceBaseUrl
    ) {
        this.restTemplate = restTemplate;
        this.studyServiceBaseUrl = studyServiceBaseUrl;
    }

    public Object[] getJoinedGroups(Long userId) {
        String url = studyServiceBaseUrl + "/internal/study/groups/" + userId;
        return restTemplate.getForObject(url, Object[].class);
    }
}
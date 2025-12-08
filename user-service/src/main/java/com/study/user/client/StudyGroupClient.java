package com.study.user.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class StudyGroupClient {

    private final RestTemplate restTemplate;

    @Value("${study-service.base-url}")
    private String studyServiceBaseUrl;

    /**
     * study-service 의 내부 API 호출:
     * GET {study-service.base-url}/internal/study/users/{userId}/groups
     *
     * 반환 타입은 그냥 Object[] 로 받아서 그대로 프론트에 넘김
     */
    public Object[] getJoinedGroups(Long userId) {
        String url = studyServiceBaseUrl + "/internal/study/users/" + userId + "/groups";
        return restTemplate.getForObject(url, Object[].class);
    }
}
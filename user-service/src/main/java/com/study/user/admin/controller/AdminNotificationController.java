package com.study.user.admin.controller;

import com.study.common.security.JwtUserInfo;
import com.study.user.admin.dto.AdminNotificationRequest;
import com.study.user.domain.User;
import com.study.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    // MSA 환경에서 notification-service 주소
    // (지금 로컬 개발 기준: 10004 포트 사용)
    private static final String NOTIFICATION_BASE_URL = "http://localhost:10004";

    public AdminNotificationController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private boolean isAdmin(JwtUserInfo user) {
        return user != null && user.isAdmin();
    }

    // 🔥 관리자: 알림 발송
    @PostMapping
    public ResponseEntity<String> sendNotification(
            @AuthenticationPrincipal JwtUserInfo userInfo,
            @RequestBody AdminNotificationRequest request
    ) {
        if (!isAdmin(userInfo)) {
            throw new AccessDeniedException("관리자만 알림을 발송할 수 있습니다.");
        }

        List<Long> userIds = request.getUserIds();

        // ⭐ 전체 발송: userIds 비어있으면 전체 사용자
        if (userIds == null || userIds.isEmpty()) {
            userIds = userRepository.findAll()
                    .stream()
                    .map(User::getUserId)
                    .toList();
        }

        // 실제로 notification-service로 넘길 body 구성
        AdminNotificationRequest forward = new AdminNotificationRequest();
        forward.setUserIds(userIds);
        forward.setMessage(request.getMessage());
        forward.setType(request.getType());

        // notification-service의 /api/notifications 엔드포인트로 POST
        ResponseEntity<String> response = restTemplate.postForEntity(
                NOTIFICATION_BASE_URL + "/api/notifications",
                forward,
                String.class
        );

        // 그대로 프론트에 응답 리턴 (JSON 문자열 그대로 통과)
        return ResponseEntity
                .status(response.getStatusCode())
                .body(response.getBody());
    }
}

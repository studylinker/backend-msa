package com.study.user.admin.controller;

import com.study.common.security.JwtUserInfo;
import com.study.user.admin.dto.AdminNotificationRequest;
import com.study.user.domain.User;
import com.study.user.notificationclient.NotificationClient;
import com.study.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final UserRepository userRepository;
    private final NotificationClient notificationClient;

    public AdminNotificationController(
            UserRepository userRepository,
            NotificationClient notificationClient
    ) {
        this.userRepository = userRepository;
        this.notificationClient = notificationClient;
    }

    private boolean isAdmin(JwtUserInfo user) {
        return user != null && user.isAdmin();
    }

    @PostMapping
    public ResponseEntity<String> sendNotification(
            @AuthenticationPrincipal JwtUserInfo userInfo,
            @RequestBody AdminNotificationRequest request
    ) {
        if (!isAdmin(userInfo)) {
            throw new AccessDeniedException("관리자만 알림을 발송할 수 있습니다.");
        }

        List<Long> userIds = request.getUserIds();

        // 전체 사용자면 user-service DB 조회
        if (userIds == null || userIds.isEmpty()) {
            userIds = userRepository.findAll()
                    .stream()
                    .map(User::getUserId)
                    .toList();
        }

        // forwarding DTO 만들어서 notification-service 로 전송
        AdminNotificationRequest forward = new AdminNotificationRequest();
        forward.setUserIds(userIds);
        forward.setMessage(request.getMessage());
        forward.setType(request.getType());

        notificationClient.send(forward);

        return ResponseEntity.ok("알림 발송 완료");
    }
}

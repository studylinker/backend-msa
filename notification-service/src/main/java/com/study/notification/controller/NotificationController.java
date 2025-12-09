package com.study.notification.controller;

import com.study.common.security.JwtUserInfo;
import com.study.notification.dto.NotificationRequest;
import com.study.notification.dto.NotificationResponse;
import com.study.notification.service.NotificationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    // 🔹 내 알림 전체 조회
    @GetMapping
    public List<NotificationResponse> getAll(@AuthenticationPrincipal JwtUserInfo user) {
        Long userId = user.getUserId();
        return service.findAllResponsesByUser(userId);
    }

    // 🔹 내 읽지 않은 알림 조회
    @GetMapping("/unread")
    public List<NotificationResponse> getUnread(@AuthenticationPrincipal JwtUserInfo user) {
        Long userId = user.getUserId();
        return service.findUnreadResponsesByUser(userId);
    }

    // 🔹 알림 생성 (관리자 제한 제거)
    @PostMapping
    public List<NotificationResponse> create(
            @RequestBody NotificationRequest body
    ) {
        List<Long> userIds = body.getUserIds();

        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("알림 대상 userIds는 최소 1명 이상 필요합니다.");
        }

        return userIds.stream()
                .map(userId -> service.save(userId, body))
                .toList();
    }

    // 🔹 내 알림 읽음 처리
    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        Long userId = user.getUserId();
        return service.markAsRead(id, userId);
    }

    // 🔹 내 알림 단건 삭제
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserInfo user
    ) {
        Long userId = user.getUserId();
        service.deleteById(id, userId);
    }

    // 🔹 내 알림 전체 삭제
    @DeleteMapping("/all")
    public void deleteAll(@AuthenticationPrincipal JwtUserInfo user) {
        Long userId = user.getUserId();
        service.deleteAllByUser(userId);
    }

    // (선택) 디버그용으로 /me 남겨두고 싶으면:
    // @GetMapping("/me")
    // public JwtUserInfo me(@AuthenticationPrincipal JwtUserInfo user) {
    //     return user;
    // }
}

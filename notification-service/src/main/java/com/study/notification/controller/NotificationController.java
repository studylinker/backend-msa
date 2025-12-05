package com.study.notification.controller;

import com.study.notification.dto.NotificationRequest;
import com.study.notification.dto.NotificationResponse;
import com.study.notification.service.NotificationService;
import com.study.common.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;
    private final JwtTokenProvider jwtTokenProvider;

    public NotificationController(NotificationService service,
                                  JwtTokenProvider jwtTokenProvider) {
        this.service = service;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // 내부 유틸: JWT 토큰 문자열 추출
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new AccessDeniedException("Authorization 헤더가 없습니다.");
        }
        return header.substring(7); // 'Bearer ' 제거 후 토큰만 반환
    }

    // 내부 유틸: userId 추출
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = extractToken(request);
        return jwtTokenProvider.getUserId(token);
    }

    // 🔹 GET /api/notifications - 내 알림 목록 조회
    @GetMapping
    public List<NotificationResponse> getAll(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return service.findAllResponsesByUser(userId);
    }

    // 🔹 GET /api/notifications/unread - 내 읽지 않은 알림 조회
    @GetMapping("/unread")
    public List<NotificationResponse> getUnread(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        return service.findUnreadResponsesByUser(userId);
    }

    // 🔹 POST /api/notifications - 관리자에 의한 강제 알림 생성
    @PostMapping
    public List<NotificationResponse> create(
            HttpServletRequest request,
            @RequestBody NotificationRequest body
    ) {
        // 1) 토큰 꺼내기
        String token = extractToken(request);

        // 2) userId도 필요하면 추출 가능
        Long adminId = jwtTokenProvider.getUserId(token);

        // 3) 관리자(role == ADMIN) 아니면 차단
//        if (!jwtTokenProvider.hasAdminRole(token)) {
//            throw new AccessDeniedException("알림 생성은 관리자만 가능합니다.");
//        }

        // 4) 요청 검증
        List<Long> userIds = body.getUserIds();
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("알림 대상 userIds는 최소 1명 이상 필요합니다.");
        }

        // 5) 알림 생성
        return userIds.stream()
                .map(userId -> service.save(userId, body))
                .toList();
    }

    // 🔹 PATCH /api/notifications/{id}/read - 내 알림 읽음 처리
    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        return service.markAsRead(id, userId);
    }

    // 🔹 DELETE /api/notifications/{id} - 내 알림 단건 삭제
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long userId = getUserIdFromRequest(request);
        service.deleteById(id, userId);
    }

    // 🔹 DELETE /api/notifications/all - 내 알림 전체 삭제
    @DeleteMapping("/all")
    public void deleteAll(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        service.deleteAllByUser(userId);
    }
}

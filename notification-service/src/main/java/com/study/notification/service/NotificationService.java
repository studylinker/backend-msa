package com.study.notification.service;

import com.study.notification.domain.Notification;
import com.study.notification.dto.NotificationRequest;
import com.study.notification.dto.NotificationResponse;
import com.study.notification.repository.NotificationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // 🔹 유저 전체 알림 조회
    @Transactional(readOnly = true)
    public List<NotificationResponse> findAllResponsesByUser(Long userId) {
        return notificationRepository
                .findByUserIdOrderByNotificationIdDesc(userId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    // 🔹 유저 읽지 않은 알림 조회
    @Transactional(readOnly = true)
    public List<NotificationResponse> findUnreadResponsesByUser(Long userId) {
        return notificationRepository
                .findByUserIdAndIsReadFalseOrderByNotificationIdDesc(userId)
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    // 🔹 알림 생성 (targetUserId = 알림 받을 유저 ID)
    @Transactional
    public NotificationResponse save(Long targetUserId, NotificationRequest request) {

        Notification notification = new Notification();
        notification.setUserId(targetUserId);
        notification.setMessage(request.getMessage());
        notification.setType(Notification.Type.valueOf(request.getType().toUpperCase()));
        notification.setIsRead(false);

        Notification saved = notificationRepository.save(notification);
        return NotificationResponse.fromEntity(saved);
    }

    // 🔹 알림 읽음 처리 (단건) - 내 알림만
    @Transactional
    public NotificationResponse markAsRead(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("알림을 찾을 수 없습니다. ID: " + id));

        if (!notification.getUserId().equals(userId)) {
            // 권한 문제이므로 AccessDeniedException 사용
            throw new AccessDeniedException("본인의 알림만 읽음 처리할 수 있습니다.");
        }

        notification.setIsRead(true);
        return NotificationResponse.fromEntity(notification);
    }

    // 🔹 알림 단건 삭제 - 내 알림만
    @Transactional
    public void deleteById(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("알림을 찾을 수 없습니다. ID: " + id));

        if (!notification.getUserId().equals(userId)) {
            throw new AccessDeniedException("본인의 알림만 삭제할 수 있습니다.");
        }

        notificationRepository.delete(notification);
    }

    // 🔹 유저 알림 전체 삭제
    @Transactional
    public void deleteAllByUser(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }
}
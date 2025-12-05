package com.study.notification.repository;

import com.study.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 특정 유저의 알림 목록 (최신순)
    List<Notification> findByUserIdOrderByNotificationIdDesc(Long userId);

    // 특정 유저의 안 읽은 알림 목록
    List<Notification> findByUserIdAndIsReadFalseOrderByNotificationIdDesc(Long userId);

    // 특정 유저의 전체 알림 삭제
    void deleteByUserId(Long userId);
}

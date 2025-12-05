package com.study.notification.dto;

import com.study.notification.domain.Notification;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long notificationId;
    private Long userId;
    private String message;
    private String type;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponse(Long notificationId,
                                Long userId,
                                String message,
                                String type,
                                Boolean isRead,
                                LocalDateTime createdAt) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public static NotificationResponse fromEntity(Notification n) {
        return new NotificationResponse(
                n.getNotificationId(),
                n.getUserId(),          // 🔥 user.getUserId() 아님
                n.getMessage(),
                n.getType().name(),
                n.getIsRead(),
                n.getCreatedAt()
        );
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

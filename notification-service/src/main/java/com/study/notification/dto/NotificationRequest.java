package com.study.notification.dto;

import java.util.List;

public class NotificationRequest {

    // 여러 명 타겟 가능
    private List<Long> userIds;

    private String message;
    private String type;   // "SCHEDULE", "REQUEST", "SYSTEM"

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

package com.study.user.admin.dto;

import java.util.List;

public class AdminNotificationRequest {

    // 알림을 보낼 대상 유저 ID들 (비우면 "전체 발송"으로 처리)
    private List<Long> userIds;
    private String message;
    private String type;   // "REQUEST" | "SCHEDULE" | "SYSTEM" 등

    public List<Long> getUserIds() { return userIds; }
    public void setUserIds(List<Long> userIds) { this.userIds = userIds; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}

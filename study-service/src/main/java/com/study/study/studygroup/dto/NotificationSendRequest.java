package com.study.study.studygroup.dto;

import java.util.List;

/**
 * notification-service 로 HTTP 전송할 때 사용하는
 * 로컬 DTO (user-service DTO를 import 하지 않기 위해 따로 정의)
 *
 * JSON 구조:
 * {
 *   "userIds": [1, 2, 3],
 *   "message": "내용",
 *   "type": "REQUEST" | "SCHEDULE" | "SYSTEM"
 * }
 */
public class NotificationSendRequest {

    private List<Long> userIds;
    private String message;
    private String type;

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

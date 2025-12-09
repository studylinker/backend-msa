package com.study.study.userclient.dto;

import java.util.List;

public class NotificationSendRequest {

    private List<Long> userIds;
    private String message;
    private String type;   // REQUEST, SCHEDULE, SYSTEM 등

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

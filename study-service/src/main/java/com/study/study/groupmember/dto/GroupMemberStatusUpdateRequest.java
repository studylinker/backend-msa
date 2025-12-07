package com.study.study.groupmember.dto;

public class GroupMemberStatusUpdateRequest {

    private String status;

    public GroupMemberStatusUpdateRequest() {
    }

    public GroupMemberStatusUpdateRequest(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
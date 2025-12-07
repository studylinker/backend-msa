package com.study.study.groupmember.dto;

public class GroupMemberRequest {
    private Long groupId;
    private Long userId;
    private String role;   // "LEADER" | "MEMBER" (선택)
    private String status; // "PENDING" | "APPROVED" | "REJECTED" (선택)

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

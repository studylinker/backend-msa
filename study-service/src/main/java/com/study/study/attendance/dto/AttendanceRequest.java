package com.study.study.attendance.dto;

public class AttendanceRequest {
    private Long scheduleId;
    private Long userId;
    private String status; // "PRESENT", "ABSENT", "LATE"

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

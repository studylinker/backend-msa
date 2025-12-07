package com.study.study.attendance.dto;

public class AttendanceStatusUpdateRequest {

    private String status; // "PRESENT", "ABSENT", "LATE"

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
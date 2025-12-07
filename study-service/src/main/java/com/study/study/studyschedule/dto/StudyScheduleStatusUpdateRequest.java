package com.study.study.studyschedule.dto;

public class StudyScheduleStatusUpdateRequest {

    private String status; // 예: "IN_PROGRESS", "COMPLETED", "CANCELLED"

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
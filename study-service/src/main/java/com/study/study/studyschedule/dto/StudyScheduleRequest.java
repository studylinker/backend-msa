package com.study.study.studyschedule.dto;

import java.time.LocalDateTime;

public class StudyScheduleRequest {

    private Long groupId;        // 🔹 그룹 ID

    private String title;
    private String description;
    private LocalDateTime startTime;  // 🔹 LocalDateTime
    private LocalDateTime endTime;    // 🔹 LocalDateTime
    private String location;

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
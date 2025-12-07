package com.study.study.studyschedule.dto;

import com.study.study.studyschedule.domain.StudySchedule;

import java.time.LocalDateTime;

public class StudyScheduleResponse {

    private Long scheduleId;
    private Long groupId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private String status;
    private LocalDateTime createdAt;

    // 🟡 변경된 도메인 기준으로 수정됨
    public StudyScheduleResponse(StudySchedule schedule) {
        this.scheduleId = schedule.getScheduleId();

        // 🟡 groupId 직접 사용 (엔티티 참조 제거)
        this.groupId = schedule.getGroupId();

        this.title = schedule.getTitle();
        this.description = schedule.getDescription();
        this.startTime = schedule.getStartTime();
        this.endTime = schedule.getEndTime();
        this.location = schedule.getLocation();

        // enum → String
        this.status = (schedule.getStatus() != null)
                ? schedule.getStatus().name()
                : null;

        this.createdAt = schedule.getCreatedAt();
    }

    public static StudyScheduleResponse fromEntity(StudySchedule schedule) {
        return new StudyScheduleResponse(schedule);
    }

    public Long getScheduleId() { return scheduleId; }
    public Long getGroupId() { return groupId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

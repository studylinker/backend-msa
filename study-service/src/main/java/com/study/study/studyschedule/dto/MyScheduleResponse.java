package com.study.study.studyschedule.dto;

import java.sql.Timestamp;

public class MyScheduleResponse {

    private Long scheduleId;
    private String title;
    private Timestamp startTime;
    private Timestamp endTime;
    private String location;
    private Long groupId;

    public MyScheduleResponse() {
    }

    public MyScheduleResponse(Long scheduleId,
                              String title,
                              Timestamp startTime,
                              Timestamp endTime,
                              String location,
                              Long groupId) {
        this.scheduleId = scheduleId;
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.groupId = groupId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public String getTitle() {
        return title;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public String getLocation() {
        return location;
    }

    public Long getGroupId() {
        return groupId;
    }
}
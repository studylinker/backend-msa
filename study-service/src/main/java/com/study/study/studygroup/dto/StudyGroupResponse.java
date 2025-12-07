package com.study.study.studygroup.dto;

import com.study.study.studygroup.domain.StudyGroup;

import java.time.LocalDateTime;

public class StudyGroupResponse {

    private Long groupId;
    private Long leaderId;
    private String title;
    private String description;
    private Integer maxMembers;

    /**
     * category: JSON 문자열
     * 예) ["Java","Spring"]
     */
    private String category;

    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;

    // 🔹 그룹 상태만 남김
    private String status;

    public StudyGroupResponse(Long groupId, Long leaderId, String title, String description,
                              Integer maxMembers, String category,
                              Double latitude, Double longitude,
                              LocalDateTime createdAt,
                              String status) {
        this.groupId = groupId;
        this.leaderId = leaderId;
        this.title = title;
        this.description = description;
        this.maxMembers = maxMembers;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
        this.status = status;
    }

    public static StudyGroupResponse fromEntity(StudyGroup group) {
        return new StudyGroupResponse(
                group.getGroupId(),
                group.getLeaderId(),  // 🟡 User → leaderId(Long) 사용
                group.getTitle(),
                group.getDescription(),
                group.getMaxMembers(),
                group.getCategory(),
                group.getLatitude() != null ? group.getLatitude().doubleValue() : null,
                group.getLongitude() != null ? group.getLongitude().doubleValue() : null,
                group.getCreatedAt(),
                group.getStatus() != null ? group.getStatus().name() : null
        );
    }

    public Long getGroupId() { return groupId; }
    public Long getLeaderId() { return leaderId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getMaxMembers() { return maxMembers; }
    public String getCategory() { return category; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getStatus() { return status; }
}

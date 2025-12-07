package com.study.study.studypost.dto;

public class StudyPostUpdateRequest {

    private String title;
    private String content;
    private String location;

    private Integer maxMembers;
    private String studyDate; // 수정 시에만 전달
    private String type;      // "FREE", "STUDY", "REVIEW", "NOTICE"

    // 현재 인원
    private Integer currentMembers;

    // 그룹 변경 (선택)
    private Long groupId;

    // 위치 수정
    private Double latitude;
    private Double longitude;

    // ✅ 신고 관련 필드
    private Boolean reported;
    private String reportReason;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }

    public String getStudyDate() { return studyDate; }
    public void setStudyDate(String studyDate) { this.studyDate = studyDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getCurrentMembers() { return currentMembers; }
    public void setCurrentMembers(Integer currentMembers) { this.currentMembers = currentMembers; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Boolean getReported() { return reported; }
    public void setReported(Boolean reported) { this.reported = reported; }

    public String getReportReason() { return reportReason; }
    public void setReportReason(String reportReason) { this.reportReason = reportReason; }
}
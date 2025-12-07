package com.study.study.studypost.dto;

public class StudyPostCreateRequest {

    private Long leaderId;      // 리더 유저 ID
    private Long groupId;       // 💡 새로 추가: 그룹 ID (선택)

    private String title;
    private String content;
    private String location;

    private Integer maxMembers; // null이면 서비스단에서 기본값 0 처리 가능
    private String studyDate;   // "yyyy-MM-dd HH:mm:ss" 형식으로 받는다고 가정

    private String type;        // "FREE", "STUDY", "REVIEW"

    // 💡 새로 추가: 위도/경도
    private Double latitude;
    private Double longitude;

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public String getStudyDate() {
        return studyDate;
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = studyDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
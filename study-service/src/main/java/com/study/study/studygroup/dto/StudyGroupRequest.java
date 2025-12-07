package com.study.study.studygroup.dto;

public class StudyGroupRequest {

    private String title;
    private String description;
    private Integer maxMembers;

    /**
     * category는 JSON 문자열로 받는다고 가정
     * 예) ["Java","Spring"]
     */
    private String category;

    private Double latitude;
    private Double longitude;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
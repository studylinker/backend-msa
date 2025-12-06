package com.study.recommend.dto;

public class PopularLocationGroupDto {

    private Long groupId;
    private String title;
    private String description;

    private long memberCount;
    private Integer maxMembers;
    private String status;

    private Double latitude;
    private Double longitude;
    private Double distanceKm;

    private Double popScore;
    private Double distanceScore;
    private Double finalScore;

    // 기본 생성자
    public PopularLocationGroupDto() {}

    // 전체 필드 생성자
    public PopularLocationGroupDto(Long groupId, String title, String description,
                                   long memberCount, Integer maxMembers, String status,
                                   Double latitude, Double longitude, Double distanceKm,
                                   Double popScore, Double distanceScore, Double finalScore) {

        this.groupId = groupId;
        this.title = title;
        this.description = description;
        this.memberCount = memberCount;
        this.maxMembers = maxMembers;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = distanceKm;
        this.popScore = popScore;
        this.distanceScore = distanceScore;
        this.finalScore = finalScore;
    }

    // getter들
    public Long getGroupId() { return groupId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getMemberCount() { return memberCount; }
    public Integer getMaxMembers() { return maxMembers; }
    public String getStatus() { return status; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Double getDistanceKm() { return distanceKm; }
    public Double getPopScore() { return popScore; }
    public Double getDistanceScore() { return distanceScore; }
    public Double getFinalScore() { return finalScore; }
}

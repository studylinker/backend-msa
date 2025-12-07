package com.study.study.studypost.dto;

import com.study.study.studypost.domain.BoardType;
import com.study.study.studypost.domain.StudyPost;
import java.time.LocalDateTime;

public class StudyPostResponse {

    private Long postId;
    private String title;
    private String content;
    private String location;
    private int maxMembers;
    private int currentMembers;
    private LocalDateTime studyDate;

    private Long leaderId;     // 🟡 엔티티에서 직접 가져옴 (User 제거됨)
    private String leaderName; // 🟡 user-service 호출로 채워질 값 (fromEntity에서는 null)

    private Long groupId;      // 🟡 StudyGroup 제거됨 → ID 기반
    private Double latitude;
    private Double longitude;

    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean reported;
    private String reportReason;

    // ================================
    // 🟡 엔티티 → DTO 변환 (MSA 기준 맞춤)
    // ================================
    public static StudyPostResponse fromEntity(StudyPost post) {
        StudyPostResponse dto = new StudyPostResponse();

        dto.postId = post.getPostId();
        dto.title = post.getTitle();
        dto.content = post.getContent();
        dto.location = post.getLocation();
        dto.maxMembers = post.getMaxMembers();
        dto.currentMembers = post.getCurrentMembers();
        dto.studyDate = post.getStudyDate();
        dto.createdAt = post.getCreatedAt();
        dto.updatedAt = post.getUpdatedAt();

        // 🟡 leaderId는 엔티티가 직접 가지고 있음
        dto.leaderId = post.getLeaderId();

        // 🟡 leaderName은 user-service 조회가 필요한 값 → 여기서는 null 유지
        dto.leaderName = null;

        // 🟡 groupId도 엔티티에 직접 존재
        dto.groupId = post.getGroupId();

        dto.latitude = post.getLatitude();
        dto.longitude = post.getLongitude();

        BoardType type = post.getType();
        dto.type = (type != null) ? type.name() : null;

        dto.reported = post.getReported();
        dto.reportReason = post.getReportReason();

        return dto;
    }

    // ===== getter =====
    public Long getPostId() { return postId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getLocation() { return location; }
    public int getMaxMembers() { return maxMembers; }
    public int getCurrentMembers() { return currentMembers; }
    public LocalDateTime getStudyDate() { return studyDate; }
    public Long getLeaderId() { return leaderId; }
    public String getLeaderName() { return leaderName; }
    public Long getGroupId() { return groupId; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getType() { return type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Boolean getReported() { return reported; }
    public String getReportReason() { return reportReason; }
}

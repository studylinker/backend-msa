package com.study.study.studypost.dto;

import com.study.study.studypost.domain.StudyReview;

import java.time.LocalDateTime;

public class StudyReviewResponse {

    private Long reviewId;
    private Long postId;   // 🟡 StudyReview.postId
    private Long userId;   // 🟡 StudyReview.userId
    private String userName;  // 🟡 user-service 호출로 채워질 값
    private int rating;
    private String content;
    private LocalDateTime createdAt;

    public static StudyReviewResponse fromEntity(StudyReview review) {
        StudyReviewResponse dto = new StudyReviewResponse();

        dto.reviewId = review.getReviewId();
        dto.postId = review.getPostId();  // 🟡 연관 제거로 단순 ID 사용
        dto.userId = review.getUserId();  // 🟡 연관 제거로 단순 ID 사용

        // 🟡 user-service 호출로 채워야 하므로 기본값 null
        dto.userName = null;

        dto.rating = review.getRating();
        dto.content = review.getContent();
        dto.createdAt = review.getCreatedAt();

        return dto;
    }

    public Long getReviewId() { return reviewId; }
    public Long getPostId() { return postId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public int getRating() { return rating; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

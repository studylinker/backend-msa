package com.study.study.studypost.dto;

public class StudyReviewCreateRequest {

    private Long userId;   // 리뷰 작성자 ID (users.user_id)
    private int rating;    // 1~5
    private String content;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

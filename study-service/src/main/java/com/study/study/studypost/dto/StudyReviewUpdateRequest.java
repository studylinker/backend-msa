package com.study.study.studypost.dto;

public class StudyReviewUpdateRequest {

    private Integer rating;   // null이면 변경 안 함
    private String content;   // null이면 변경 안 함

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
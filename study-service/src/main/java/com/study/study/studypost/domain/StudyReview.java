package com.study.study.studypost.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Study_reviews")
@Getter
@Setter
public class StudyReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    // ================================
    // 🟡 MSA 규칙에 맞게 수정된 부분
    // ================================

    // ❌ 기존:
    // @ManyToOne
    // @JoinColumn(name = "post_id")
    // private StudyPost post;
    //
    // @ManyToOne
    // @JoinColumn(name = "user_id")
    // private User user;
    //
    // → 다른 서비스(User) + 연관관계 의존

    // ✅ 변경: ID만 저장
    @Column(name = "post_id", nullable = false)
    private Long postId;   // 어떤 게시글에 대한 리뷰인지

    @Column(name = "user_id", nullable = false)
    private Long userId;   // 누가 작성했는지 (user-service ID)

    // ================================

    private int rating; // 1~5점

    @Lob
    private String content;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

package com.study.study.studypost.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "Study_posts")
@Getter
@Setter
@DynamicUpdate
public class StudyPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")   // ★ PK 컬럼명 유지
    private Long postId;

    private String title;

    @Lob
    private String content;

    @Column(name = "max_members")
    private int maxMembers;

    @Column(name = "current_members")
    private int currentMembers = 0;

    private String location;

    @Column(name = "study_date")
    private LocalDateTime studyDate;

    // ================================
    // 🟡 MSA 규칙에 맞게 수정된 부분
    // ================================

    // ❌ 기존: @ManyToOne User leader
    //    → 다른 서비스 도메인(User)을 직접 참조해서 MSA 위반
    // ✅ 변경: leaderId(Long)만 보관
    @Column(name = "leader_id", nullable = false)
    private Long leaderId;

    // ❌ 기존: @ManyToOne StudyGroup group
    //    → study-service ↔ 다른 서비스 의존 꼬임
    // ✅ 변경: groupId(Long)만 보관
    @Column(name = "group_id")
    private Long groupId;

    // ❌ 기존: @OneToMany(mappedBy = "post") List<StudyReview> reviews;
    //    → 양방향 연관관계는 굳이 필요 없음 (postId로 리뷰 조회 가능)
    // ✅ 제거: 리뷰는 StudyReviewRepository에서 postId 기반으로 조회

    @Enumerated(EnumType.STRING)
    private BoardType type; // FREE, STUDY, REVIEW, NOTICE

    private Double latitude;
    private Double longitude;

    // 🔽 신고 상태
    @Column(name = "reported")
    private Boolean reported = false;

    @Column(name = "report_reason")
    private String reportReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}

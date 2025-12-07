package com.study.study.studypost.repository;

import com.study.study.studypost.domain.StudyReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyReviewRepository extends JpaRepository<StudyReview, Long> {

    // 🟡 수정됨: 엔티티 연관 제거 → postId 기반으로 조회
    List<StudyReview> findByPostId(Long postId);
}

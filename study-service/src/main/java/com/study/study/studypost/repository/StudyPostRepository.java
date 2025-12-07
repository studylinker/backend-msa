package com.study.study.studypost.repository;

import com.study.study.studypost.domain.StudyPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyPostRepository extends JpaRepository<StudyPost, Long> {
}
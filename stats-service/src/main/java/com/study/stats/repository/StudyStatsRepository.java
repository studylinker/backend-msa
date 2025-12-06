package com.study.stats.repository;

import com.study.studygroup.domain.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyStatsRepository extends JpaRepository<StudyGroup, Long> {

    // 월별 스터디 생성 수
    @Query(
            value = "SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, COUNT(*) " +
                    "FROM Study_groups GROUP BY month ORDER BY month",
            nativeQuery = true
    )
    List<Object[]> getMonthlyStudyCount();
}


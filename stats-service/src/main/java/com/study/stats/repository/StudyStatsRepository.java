package com.study.stats.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 스터디 관련 통계 조회용 레포지토리
 * - Study_groups 테이블 기준 월별 스터디 생성 수 집계
 */
@Repository
public class StudyStatsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 월별 스터디 생성 수 조회
     * - 결과: [month(YYYY-MM), count]
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getMonthlyStudyCount() {
        String sql = "SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, COUNT(*) " +
                "FROM Study_groups " +
                "GROUP BY month " +
                "ORDER BY month";
        return entityManager.createNativeQuery(sql).getResultList();
    }
}
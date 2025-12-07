package com.study.stats.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 출석 통계 조회용 레포지토리
 * - Attendance 테이블의 status 컬럼 기준으로 그룹화
 */
@Repository
public class AttendanceStatsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<Object[]> getAttendanceRatio() {
        String sql = "SELECT status, COUNT(*) FROM Attendance GROUP BY status";
        return entityManager.createNativeQuery(sql).getResultList();
    }
}
package com.study.stats.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

/**
 * 대시보드 상단 요약 통계 조회용 레포지토리
 * - Users, Study_groups 테이블을 직접 native query로 조회
 */
@Repository
public class SummaryStatsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 전체 사용자 수
     * - Users 테이블 전체 count
     */
    public long countTotalUsers() {
        String sql = "SELECT COUNT(*) FROM Users";
        Number count = (Number) entityManager
                .createNativeQuery(sql)
                .getSingleResult();
        return count.longValue();
    }

    /**
     * 활성 스터디 수
     * - Study_groups.status = 'ACTIVE' 인 row 수
     */
    public long countActiveStudies() {
        String sql = "SELECT COUNT(*) FROM Study_groups WHERE status = 'ACTIVE'";
        Number count = (Number) entityManager
                .createNativeQuery(sql)
                .getSingleResult();
        return count.longValue();
    }

    /**
     * 오늘 가입한 신규 유저 수
     * - Users.created_at 의 DATE 부분이 오늘(CURDATE()) 인 row 수
     */
    public long countNewUsersToday() {
        String sql = "SELECT COUNT(*) FROM Users WHERE DATE(created_at) = CURDATE()";
        Number count = (Number) entityManager
                .createNativeQuery(sql)
                .getSingleResult();
        return count.longValue();
    }
}
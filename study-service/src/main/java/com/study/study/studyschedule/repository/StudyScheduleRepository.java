package com.study.study.studyschedule.repository;

import com.study.study.studyschedule.domain.StudySchedule;
import com.study.study.studyschedule.dto.MyScheduleResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;

public interface StudyScheduleRepository extends JpaRepository<StudySchedule, Long> {

    // 오늘 일정
    List<StudySchedule> findByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime start, LocalDateTime end);

    // 다가올 일정
    List<StudySchedule> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime now);

    // 🟡 그룹 일정: groupId 기반으로 변경
    List<StudySchedule> findByGroupId(Long groupId);

    // ================================
    // "내 일정" (native query 유지)
    // ================================
    @Query(
            value = """
        SELECT
            s.schedule_id,
            s.title,
            s.start_time,
            s.end_time,
            s.location,
            s.group_id
        FROM Study_schedules s
        WHERE 
            s.user_id = :userId
            OR s.group_id IN (
                SELECT gm.group_id
                FROM Group_members gm
                WHERE gm.user_id = :userId
                AND gm.status = 'APPROVED'
            )
        ORDER BY s.start_time DESC
        """,
            nativeQuery = true
    )
    List<Object[]> findRawMySchedules(Long userId);

    // ================================
    // 변환 헬퍼 (기존 유지)
    // ================================
    default List<MyScheduleResponse> getMySchedules(Long userId) {
        List<Object[]> rows = findRawMySchedules(userId);

        return rows.stream().map(r -> {
            Long gid = (r[5] == null ? null : ((Number) r[5]).longValue());

            return new MyScheduleResponse(
                    ((Number) r[0]).longValue(),
                    (String) r[1],
                    (Timestamp) r[2],
                    (Timestamp) r[3],
                    (String) r[4],
                    gid
            );
        }).toList();
    }
}

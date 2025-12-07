package com.study.study.attendance.repository;

import com.study.study.attendance.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // 🔥 기존 모놀리식 코드: 엔티티 기반 검색
    // Optional<Attendance> findByScheduleAndUser(StudySchedule schedule, User user);
    // → MSA에서는 StudySchedule/User 엔티티를 참조하면 안 되므로 제거

    // 🔥 기존 모놀리식 코드: 엔티티 경로 기반 검색
    // List<Attendance> findBySchedule_ScheduleId(Long scheduleId);
    // → MSA에서는 Attendance 엔티티 자체가 scheduleId를 가지므로 필요 없음

    // 🔥 기존 모놀리식 코드:
    // List<Attendance> findByUser_UserId(Long userId);
    // → user 엔티티를 참조하므로 제거

    // ================================
    // ⭐ MSA 정답: 숫자(Long) 기반 검색 메소드
    // ================================

    // 🔥 scheduleId와 userId를 기준으로 단일 출석 조회 (업데이트 시 사용)
    Optional<Attendance> findByScheduleIdAndUserId(Long scheduleId, Long userId);

    // 🔥 스케줄별 출석 조회
    List<Attendance> findByScheduleId(Long scheduleId);

    // 🔥 사용자별 출석 조회
    List<Attendance> findByUserId(Long userId);
}

package com.study.study.attendance.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "Attendance",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"schedule_id", "user_id"})}
)
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long attendanceId;

    // ❌ 기존: @ManyToOne StudySchedule schedule
    // 🔥 MSA 구조에서는 외부 엔티티 참조를 지양 → scheduleId(Long)로 변경
    @Column(name = "schedule_id", nullable = false)
    private Long scheduleId;   // 🔥 변경됨

    // ❌ 기존: @ManyToOne User user
    // 🔥 MSA는 user 엔티티 절대 직접 참조 금지 → userId(Long)만 저장
    @Column(name = "user_id", nullable = false)
    private Long userId;       // 🔥 변경됨

    @Enumerated(EnumType.STRING)
    private Status status = Status.ABSENT;

    @Column(name = "checked_at")
    private LocalDateTime checkedAt = LocalDateTime.now();

    public enum Status {
        PRESENT,
        ABSENT,
        LATE
    }

    // ===== Getter/Setter =====
    public Long getAttendanceId() {
        return attendanceId;
    }

    public void setAttendanceId(Long attendanceId) {
        this.attendanceId = attendanceId;
    }

    public Long getScheduleId() {   // 🔥 getter 이름도 scheduleId 유지
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {   // 🔥 setter 추가
        this.scheduleId = scheduleId;
    }

    public Long getUserId() {   // 🔥 User user → userId(Long)
        return userId;
    }

    public void setUserId(Long userId) {   // 🔥 setter 변경
        this.userId = userId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(LocalDateTime checkedAt) {
        this.checkedAt = checkedAt;
    }
}

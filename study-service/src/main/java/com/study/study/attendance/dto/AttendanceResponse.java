package com.study.study.attendance.dto;

import java.time.LocalDateTime;
import com.study.study.attendance.domain.Attendance;

public class AttendanceResponse {

    private Long attendanceId;
    private Long scheduleId;
    private Long userId;
    private String status;
    private LocalDateTime checkedAt;

    public AttendanceResponse(Long attendanceId,
                              Long scheduleId,
                              Long userId,
                              String status,
                              LocalDateTime checkedAt) {
        this.attendanceId = attendanceId;
        this.scheduleId = scheduleId;
        this.userId = userId;
        this.status = status;
        this.checkedAt = checkedAt;
    }

    public static AttendanceResponse fromEntity(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getAttendanceId(),
                attendance.getScheduleId(),   // 🔥 attendance.getSchedule().getScheduleId() → 변경됨
                attendance.getUserId(),       // 🔥 attendance.getUser().getUserId() → 변경됨
                attendance.getStatus().name(),
                attendance.getCheckedAt()
        );
    }

    public Long getAttendanceId() { return attendanceId; }
    public Long getScheduleId() { return scheduleId; }
    public Long getUserId() { return userId; }
    public String getStatus() { return status; }
    public LocalDateTime getCheckedAt() { return checkedAt; }
}

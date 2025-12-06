package com.study.stats.repository;

import com.study.attendance.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceStatsRepository extends JpaRepository<Attendance, Long> {

    @Query(
            value = "SELECT status, COUNT(*) FROM Attendance GROUP BY status",
            nativeQuery = true
    )
    List<Object[]> getAttendanceRatio();
}


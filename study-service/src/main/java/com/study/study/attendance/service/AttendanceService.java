package com.study.study.attendance.service;

import com.study.study.attendance.domain.Attendance;
import com.study.study.attendance.dto.AttendanceRequest;
import com.study.study.attendance.dto.AttendanceResponse;
import com.study.study.attendance.dto.AttendanceStatusUpdateRequest;
import com.study.study.attendance.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private final AttendanceRepository repository;

    public AttendanceService(AttendanceRepository repository) {
        this.repository = repository;
    }

    // ================================
    // 전체 조회 (관리자 전용)
    // ================================
    @Transactional(readOnly = true)
    public List<AttendanceResponse> findAll() {
        return repository.findAll().stream()
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ================================
    // 출석 체크 (리더만 호출 가능)
    // 컨트롤러에서 leaderId 체크 완료 → 여기서는 신뢰하고 사용
    // ================================
    @Transactional
    public AttendanceResponse checkIn(AttendanceRequest request, Long leaderId) { // 🟡 leaderId 추가

        // 기존 출석 여부 확인
        Attendance existing = repository
                .findByScheduleIdAndUserId(request.getScheduleId(), request.getUserId())
                .orElse(null);

        Attendance attendance;

        if (existing != null) {
            // 기존 출석 수정
            attendance = existing;
            attendance.setStatus(Attendance.Status.valueOf(request.getStatus()));
            attendance.setCheckedAt(LocalDateTime.now());
        } else {
            // 신규 출석 생성
            attendance = new Attendance();
            attendance.setScheduleId(request.getScheduleId());
            attendance.setUserId(request.getUserId());
            attendance.setStatus(Attendance.Status.valueOf(request.getStatus()));
            attendance.setCheckedAt(LocalDateTime.now());
        }

        Attendance saved = repository.save(attendance);
        return AttendanceResponse.fromEntity(saved);
    }

    // ================================
    // 스케줄별 조회
    // ================================
    @Transactional(readOnly = true)
    public List<AttendanceResponse> findByScheduleForLeader(Long scheduleId, Long leaderId) { // 🟡 leaderId 추가(컨트롤러 신뢰)
        return repository.findByScheduleId(scheduleId)
                .stream()
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ================================
    // 사용자별 조회
    // ================================
    @Transactional(readOnly = true)
    public List<AttendanceResponse> findByUser(Long userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(AttendanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ================================
    // 출석 상태 변경 (리더만 허용됨)
    // ================================
    @Transactional
    public AttendanceResponse updateStatus(Long attendanceId,
                                           AttendanceStatusUpdateRequest request,
                                           Long leaderId) { // 🟡 leaderId 추가

        Attendance attendance = repository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("출석 기록 없음"));

        attendance.setStatus(Attendance.Status.valueOf(request.getStatus()));
        attendance.setCheckedAt(LocalDateTime.now());

        return AttendanceResponse.fromEntity(repository.save(attendance));
    }
}

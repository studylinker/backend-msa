package com.study.study.studyschedule.service;

import com.study.study.studyschedule.domain.StudySchedule;
import com.study.study.studyschedule.domain.StudyScheduleStatus;
import com.study.study.studyschedule.dto.MyScheduleResponse;
import com.study.study.studyschedule.dto.StudyScheduleRequest;
import com.study.study.studyschedule.dto.StudyScheduleStatusUpdateRequest;
import com.study.study.studyschedule.repository.StudyScheduleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudyScheduleService {

    private final StudyScheduleRepository scheduleRepository;

    public StudyScheduleService(StudyScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    // ================================
    // 단건 조회
    // ================================
    public StudySchedule findById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("스터디 스케줄을 찾을 수 없습니다. ID: " + scheduleId)
                );
    }

    // ================================
    // 일정 생성 (userId만 저장)
    // ================================
    @Transactional
    public StudySchedule save(StudyScheduleRequest request, Long ownerId) {
        StudySchedule schedule = new StudySchedule();

        schedule.setUserId(ownerId);        // 🟡 User 엔티티 제거 → userId만 저장
        schedule.setGroupId(request.getGroupId());

        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setLocation(request.getLocation());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());

        return scheduleRepository.save(schedule);
    }

    // ================================
    // 일정 수정 (owner or leader)
    // ================================
    @Transactional
    public StudySchedule update(Long scheduleId,
                                StudyScheduleRequest request,
                                Long loginUserId) {

        StudySchedule schedule = findById(scheduleId);

        // 🟡 owner 체크
        boolean isOwner = schedule.getUserId().equals(loginUserId);

        // 🟡 leader 체크는 컨트롤러에서 인증된 사용자만 들어올 수 있음
        boolean isLeader = false;

        if (!isOwner && !isLeader) {
            throw new SecurityException("일정 수정 권한이 없습니다.");
        }

        // groupId 변경 허용
        if (request.getGroupId() != null) {
            schedule.setGroupId(request.getGroupId());
        }

        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setLocation(request.getLocation());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());

        return scheduleRepository.save(schedule);
    }

    // ================================
    // 일정 삭제
    // ================================
    @Transactional
    public void deleteById(Long scheduleId, Long loginUserId) {
        StudySchedule schedule = findById(scheduleId);

        boolean isOwner = schedule.getUserId().equals(loginUserId);
        boolean isLeader = false; // 🟡 leader 여부는 컨트롤러가 보장

        if (!isOwner && !isLeader) {
            throw new SecurityException("일정 삭제 권한이 없습니다.");
        }

        scheduleRepository.delete(schedule);
    }

    // ================================
    // 상태 변경 (리더만)
    // ================================
    @Transactional
    public StudySchedule updateStatus(Long scheduleId,
                                      StudyScheduleStatusUpdateRequest request,
                                      Long loginUserId) {

        StudySchedule schedule = findById(scheduleId);

        boolean isLeader = false; // 🟡 MSA 구조에서 leader 판단은 컨트롤러가 수행

        if (!isLeader) {
            throw new SecurityException("해당 스터디 그룹 리더만 일정 상태를 변경할 수 있습니다.");
        }

        String statusStr = request.getStatus();
        if (statusStr == null || statusStr.isBlank()) {
            throw new IllegalArgumentException("status 값이 비어 있습니다.");
        }

        try {
            StudyScheduleStatus newStatus =
                    StudyScheduleStatus.valueOf(statusStr.toUpperCase());
            schedule.setStatus(newStatus);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 상태 값입니다: " + statusStr);
        }

        return scheduleRepository.save(schedule);
    }

    // ================================
    // 특정 유저의 일정 조회
    // ================================
    public List<MyScheduleResponse> getMySchedules(Long userId) {
        return scheduleRepository.getMySchedules(userId);
    }
}

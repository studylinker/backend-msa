package com.study.study.studyschedule.service;

import com.study.study.studygroup.domain.StudyGroup;
import com.study.study.studygroup.repository.StudyGroupRepository; // 🔹 추가
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
    private final StudyGroupRepository studyGroupRepository; // 🔹 추가

    public StudyScheduleService(StudyScheduleRepository scheduleRepository,
                                StudyGroupRepository studyGroupRepository) { // 🔹 생성자 수정
        this.scheduleRepository = scheduleRepository;
        this.studyGroupRepository = studyGroupRepository;
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

        schedule.setUserId(ownerId);        // 🟡 일정 만든 사람
        schedule.setGroupId(request.getGroupId());

        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setLocation(request.getLocation());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());

        return scheduleRepository.save(schedule);
    }

    // ================================
    // 일정 수정 (owner만)
    // ================================
    @Transactional
    public StudySchedule update(Long scheduleId,
                                StudyScheduleRequest request,
                                Long loginUserId) {

        StudySchedule schedule = findById(scheduleId);

        // 🟡 owner 체크
        boolean isOwner = schedule.getUserId().equals(loginUserId);
        boolean isLeader = false; // 지금은 리더 권한은 여기서 안 씀

        if (!isOwner && !isLeader) {
            throw new SecurityException("일정 수정 권한이 없습니다.");
        }

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
        boolean isLeader = false; // 필요하면 나중에 리더도 허용 가능

        if (!isOwner && !isLeader) {
            throw new SecurityException("일정 삭제 권한이 없습니다.");
        }

        scheduleRepository.delete(schedule);
    }

    // ================================
    // 상태 변경 (🔹 그룹 리더만)
    // ================================
    @Transactional
    public StudySchedule updateStatus(Long scheduleId,
                                      StudyScheduleStatusUpdateRequest request,
                                      Long loginUserId) {

        StudySchedule schedule = findById(scheduleId);

        // 1️⃣ 이 일정이 어떤 그룹에 속해 있는지 확인
        Long groupId = schedule.getGroupId();
        if (groupId == null) {
            throw new IllegalStateException("그룹이 지정되지 않은 일정은 상태를 변경할 수 없습니다.");
        }

        // 2️⃣ 그룹 조회 후, 리더인지 확인
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("스터디 그룹을 찾을 수 없습니다. id=" + groupId));

        Long leaderId = group.getLeaderId(); // ✅ 네 도메인 기준
        boolean isLeader = leaderId != null && leaderId.equals(loginUserId);

        if (!isLeader) {
            throw new SecurityException("해당 스터디 그룹 리더만 일정 상태를 변경할 수 있습니다.");
        }

        // 3️⃣ status 값 검증 & 반영
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